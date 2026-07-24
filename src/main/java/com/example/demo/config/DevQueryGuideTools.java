package com.example.demo.config;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DevQueryGuideTools {

  @Tool(description = "Retrieves the core platform rules and guidelines of DevQuery, including how to post questions, earn reputation points, and send feedback. Useful for guiding users on how to use the platform.")
  public String getPlatformGuidelines() {
    return """
        # DevQuery Platform Guidelines for AI Assistant

        This document contains the core rules and workflows of the DevQuery platform. The AI Assistant must use this information to guide users when they ask about platform features.

        ## 1. How to Post a New Question
        * **Prerequisite:** Users must be logged into their accounts.
        * **Steps:**
          1. Click the **"Ask Question"** button located at the Home page.
          2. Enter a clear and concise **Title** summarizing the problem.
          3. Provide the detailed problem description in the **Body** section. (Markdown formatting is fully supported for code blocks and text styling).
          4. Add at least one relevant **Tag** to categorize the question.
          5. Click **"Post your question"** to submit.

        ## 2. How to Earn Reputation Points
        Reputation points measure a user's trust and contribution to the DevQuery community. Users gain or lose points based on community interactions:
        * **Upvotes:** Receiving an upvote on a question or answer grants **+10 points**.
        * **Accepted Answer:** If a user's answer is marked as "Accepted" by the question owner, they receive **+15 points**. This is the most valuable contribution.
        * **Downvotes:** Receiving a downvote results in a **-2 points** deduction.

        ## 3. How to Send Feedback
        * Users can provide suggestions or report platform issues by navigating to the **Feedbacks** page.
        * **Access:** Click the "Feedbacks" link available in the main navigation menu.
        """;
  }
}
