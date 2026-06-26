package com.example.demo.controller;

import com.example.demo.dto.BadgeDTO;
import com.example.demo.dto.PrivilegeDTO;
import com.example.demo.dto.ReputationDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.BadgeRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class BadgeController {

    @Autowired
    private UserService userService;

    @Autowired
    private BadgeRepository badgeRepository;

    @GetMapping("/badge")
    public String showBadge(
            @RequestParam(value = "id", required = false) Long idParam,
            @RequestParam(value = "tab", defaultValue = "reputation") String tab,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int pageIndex, // <-- THÊM DÒNG NÀY
            Model model) {

        Long targetUserId = idParam;

        // Retrieve the currently logged-in user from the model (injected via
        // GlobalControllerAdvice)
        User loggedInUser = (User) model.getAttribute("loggedInUser");

        // If no ID is provided in the URL, default to the logged-in user
        if (targetUserId == null) {
            if (loggedInUser != null) {
                targetUserId = loggedInUser.getUserId();
            } else {
                return "redirect:/auth/login";
            }
        }

        // Fetch target user's profile
        UserDTO uPro = userService.getUserProfileById(targetUserId);
        if (uPro == null) {
            return "redirect:/home";
        }

        model.addAttribute("uPro", uPro);
        model.addAttribute("currentTab", tab);

        // --- PROCESS DATA BASED ON SELECTED TAB ---
        if ("reputation".equals(tab)) {
            List<ReputationDTO> repList = new ArrayList<>();

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                    .of(pageIndex - 1, 10);
            org.springframework.data.domain.Page<Map<String, Object>> repPage = badgeRepository
                    .getReputationHistoryRaw(targetUserId, pageable);

            for (Map<String, Object> map : repPage.getContent()) {
                repList.add(new ReputationDTO(
                        getStringSafe(map, "action_type"),
                        getIntSafe(map, "value"),
                        getTimestampSafe(map, "created_at")));
            }
            model.addAttribute("repList", repList);
            model.addAttribute("currentPage", pageIndex);
            model.addAttribute("totalPage", repPage.getTotalPages());

        } else if ("badges".equals(tab)) {
            List<BadgeDTO> myBadges = new ArrayList<>();
            // Determine sorting strategy based on the 'sort' parameter
            List<Map<String, Object>> rawBadges = "name".equals(sort)
                    ? badgeRepository.getBadgesByReputationNameRaw(uPro.getReputation())
                    : badgeRepository.getBadgesByReputationNewestRaw(uPro.getReputation());

            for (Map<String, Object> map : rawBadges) {
                BadgeDTO b = new BadgeDTO();
                b.setName(getStringSafe(map, "name"));
                b.setType(getStringSafe(map, "type"));
                b.setDescription(getStringSafe(map, "description"));
                b.setRequiredReputation(getIntSafe(map, "required_reputation"));
                myBadges.add(b);
            }
            model.addAttribute("myBadges", myBadges);
            model.addAttribute("currentSort", sort);

        } else if ("privileges".equals(tab)) {
            List<PrivilegeDTO> privilegesList = new ArrayList<>();
            for (Map<String, Object> map : badgeRepository.getAllPrivilegesRaw()) {
                privilegesList.add(new PrivilegeDTO(
                        getIntSafe(map, "privilege_id"),
                        getStringSafe(map, "name"),
                        getStringSafe(map, "description"),
                        getIntSafe(map, "required_reputation")));
            }
            model.addAttribute("privilegesList", privilegesList);

            // Calculate progress bar logic for the next privilege level
            int currentRep = uPro.getReputation();
            PrivilegeDTO nextPrivilege = null;

            // Find the next privilege the user has not yet unlocked
            for (PrivilegeDTO priv : privilegesList) {
                if (priv.getRequiredReputation() > currentRep) {
                    nextPrivilege = priv;
                    break;
                }
            }

            if (nextPrivilege != null) {
                int targetRep = nextPrivilege.getRequiredReputation();
                int pointsNeeded = targetRep - currentRep;
                int progressPercent = (int) Math.round((currentRep * 100.0) / targetRep);

                model.addAttribute("nextPriv", nextPrivilege);
                model.addAttribute("pointsNeeded", pointsNeeded);
                model.addAttribute("progressPercent", progressPercent);
            } else {
                // User has unlocked all available privileges
                model.addAttribute("isMaxLevel", true);
            }
        }

        return "User/badge";
    }

    // =========================================================================
    // HELPER METHODS TO PREVENT SQL SERVER CASE-SENSITIVITY ISSUES WITH MAPS
    // =========================================================================

    private String getStringSafe(Map<String, Object> map, String key) {
        Object val = map.get(key.toLowerCase());
        if (val == null)
            val = map.get(key.toUpperCase());
        return val != null ? val.toString() : "";
    }

    private int getIntSafe(Map<String, Object> map, String key) {
        Object val = map.get(key.toLowerCase());
        if (val == null)
            val = map.get(key.toUpperCase());
        return val != null ? ((Number) val).intValue() : 0;
    }

    private Timestamp getTimestampSafe(Map<String, Object> map, String key) {
        Object val = map.get(key.toLowerCase());
        if (val == null)
            val = map.get(key.toUpperCase());
        if (val instanceof Timestamp)
            return (Timestamp) val;
        if (val instanceof java.util.Date)
            return new Timestamp(((java.util.Date) val).getTime());
        return null;
    }
}