# Website List — Deploy Java Spring Boot Projects

**Program:** FSA CPL Internship — Java Fullstack  
**Purpose:** Reference list of hosting platforms and database services for deploying Mock Project and CPL Project applications  
**Audience:** Any intern or team deploying a Spring Boot backend to a public URL

---

## 1\. How to Use This List

| Goal | What to do |
| :---- | :---- |
| **First-time deploy** | Start with **Render** — follow `Instruction_Using_Render.md` in this folder |
| **Compare options** | Use the tables below (free tier, database support, difficulty) |
| **Need MySQL in production** | Pick a host that supports MySQL or add an external database (Section 4\) |
| **Presentation / demo** | Any platform that gives a stable `https://` URL is fine |

> **Course recommendation:** Use **Render** for your first deployment. It has a free tier, GitHub integration, managed PostgreSQL, and a full step-by-step guide in this Mock folder.

---

## 2\. Application Hosting (Spring Boot)

Platforms that can run a Java Spring Boot app (JAR or Docker).

### 2.1 PaaS — easiest for beginners (recommended)

| Platform | URL | Free tier | Deploy from GitHub | Docker | Database on same platform | Best for |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| **Render** | [render.com](https://render.com) | Yes | Yes | Yes | PostgreSQL, Redis | **Default choice** — Mock / CPL demos |
| **Railway** | [railway.app](https://railway.app) | Limited credit | Yes | Yes | PostgreSQL, MySQL, Redis, MongoDB | Quick deploy, multiple DB types |
| **Fly.io** | [fly.io](https://fly.io) | Limited free | Yes (CLI) | Yes | Add external DB | Global regions, container-first |
| **Koyeb** | [koyeb.com](https://koyeb.com) | Yes | Yes | Yes | PostgreSQL (partner) | Simple Docker deploy |
| **Northflank** | [northflank.com](https://northflank.com) | Developer tier | Yes | Yes | Add external DB | Teams, CI/CD built-in |
| **DigitalOcean App Platform** | [digitalocean.com/products/app-platform](https://www.digitalocean.com/products/app-platform) | Trial / paid | Yes | Yes | Managed PostgreSQL | Production-style PaaS |

### 2.2 Major cloud providers — more control, steeper learning curve

| Platform | URL | Free tier | Java / Spring support | Notes |
| :---- | :---- | :---- | :---- | :---- |
| **Google Cloud Run** | [cloud.google.com/run](https://cloud.google.com/run) | Limited free | Docker / JAR in container | Serverless containers; pay per request |
| **AWS Elastic Beanstalk** | [aws.amazon.com/elasticbeanstalk](https://aws.amazon.com/elasticbeanstalk/) | 12-month free tier (EC2) | Native Java platform | Classic enterprise option |
| **Azure App Service** | [azure.microsoft.com/products/app-service](https://azure.microsoft.com/en-us/products/app-service) | Student / trial | Java 17+ runtime | Good for .NET \+ Java mixed teams |
| **Oracle Cloud (OCI)** | [oracle.com/cloud](https://www.oracle.com/cloud/) | Always-free ARM VMs | Run JAR or Docker on VM | More manual setup |
| **Google App Engine** | [cloud.google.com/appengine](https://cloud.google.com/appengine) | Limited free | Java flexible env | Older PaaS model; Cloud Run is newer |

### 2.3 VPS / VM — full server control (advanced)

| Platform | URL | Free tier | Approach |
| :---- | :---- | :---- | :---- |
| **Oracle Cloud Free Tier** | [oracle.com/cloud/free](https://www.oracle.com/cloud/free/) | Yes (ARM VM) | Install Java, run `java -jar`, configure nginx |
| **Google Cloud Compute Engine** | [cloud.google.com/compute](https://cloud.google.com/compute) | Trial credit | Same as above |
| **AWS EC2** | [aws.amazon.com/ec2](https://aws.amazon.com/ec2/) | 12-month free tier (t2.micro) | Install JDK, systemd service, security groups |
| **DigitalOcean Droplets** | [digitalocean.com/products/droplets](https://www.digitalocean.com/products/droplets) | Paid (\~$4/mo) | Linux VM \+ manual Spring Boot setup |
| **Hetzner** | [hetzner.com/cloud](https://www.hetzner.com/cloud/) | Paid (low cost) | Popular in EU for cheap VPS |

> **Intern tip:** PaaS (Section 2.1) is enough for Mock and CPL presentations. VPS (Section 2.3) is optional if you want to learn Linux server administration.

---

## 3\. Quick Comparison — Pick a Host

| If you need… | Suggested platform |
| :---- | :---- |
| Easiest first deploy \+ course guide | **Render** → `Instruction_Using_Render.md` |
| PostgreSQL \+ MySQL on one platform | **Railway** |
| Docker-only workflow | **Fly.io**, **Koyeb**, or **Render** |
| Free tier with no credit card (check current policy) | **Render**, **Fly.io** (verify at signup) |
| Enterprise / resume keyword | **AWS Elastic Beanstalk**, **Azure App Service**, **Google Cloud Run** |
| Lowest monthly cost long-term | **Oracle Cloud Free VM** or **Hetzner** (manual setup) |

---

## 4\. Database Hosting (use with any app host)

Spring Boot apps need a **persistent database** in production. Do not rely on H2 in-memory or local file DB on cloud hosts — data is lost on restart.

### 4.1 Managed PostgreSQL (recommended for Spring Boot \+ JPA)

| Provider | URL | Free tier | Pair with |
| :---- | :---- | :---- | :---- |
| **Render PostgreSQL** | [render.com](https://render.com) | Yes | Render Web Service (same dashboard) |
| **Neon** | [neon.tech](https://neon.tech) | Yes | Render, Railway, Fly.io, any host |
| **Supabase** | [supabase.com](https://supabase.com) | Yes | Any host; PostgreSQL \+ optional auth/storage |
| **Railway PostgreSQL** | [railway.app](https://railway.app) | Limited credit | Railway app or external web service |
| **ElephantSQL** | [elephantsql.com](https://www.elephantsql.com) | Tiny free plan | Any host |
| **Aiven PostgreSQL** | [aiven.io](https://aiven.io) | Trial | Production-grade option |

### 4.2 Managed MySQL (if your project uses MySQL locally)

| Provider | URL | Free tier | Notes |
| :---- | :---- | :---- | :---- |
| **Railway MySQL** | [railway.app](https://railway.app) | Limited credit | Easiest MySQL \+ app on one platform |
| **PlanetScale** | [planetscale.com](https://planetscale.com) | Limited free | Serverless MySQL; branching for schemas |
| **Aiven MySQL** | [aiven.io](https://aiven.io) | Trial | Managed MySQL |
| **ClearDB** (via Heroku add-on) | [cleardb.com](https://www.cleardb.com) | Legacy / paid | Historically used with Heroku |

> Render does **not** offer managed MySQL. Use PostgreSQL on Render, or host the app on Railway / pair Render with PlanetScale or Railway MySQL.

### 4.3 Other data stores (optional)

| Provider | URL | Use case |
| :---- | :---- | :---- |
| **Render Redis** | [render.com](https://render.com) | Caching, sessions |
| **Railway Redis** | [railway.app](https://railway.app) | Same |
| **MongoDB Atlas** | [mongodb.com/atlas](https://www.mongodb.com/atlas) | If using Spring Data MongoDB |
| **Upstash Redis** | [upstash.com](https://upstash.com) | Serverless Redis; works with any host |

---

## 5\. What Every Spring Boot Deploy Needs

Regardless of which website you choose, prepare these in your project:

| Requirement | Example |
| :---- | :---- |
| **Production profile** | `application-prod.properties` with `${ENV_VAR}` placeholders |
| **Port binding** | `server.port=${PORT:8080}` and `server.address=0.0.0.0` |
| **Database URL from env** | `spring.datasource.url=${SPRING_DATASOURCE_URL}` |
| **Secrets in env only** | API keys, DB password, admin password — not in Git |
| **Build output** | Executable JAR (`mvn package`) or `Dockerfile` |
| **Health endpoint** | `/health` or Spring Actuator `/actuator/health` |
| **JDBC driver in `pom.xml`** | `postgresql` or `mysql-connector-j` matching your DB |

---

## 6\. Suggested Stacks by Experience Level

### Beginner (Mock Project — Week 7–8)

GitHub → Render Web Service \+ Render PostgreSQL

Guide: Instruction\_Using\_Render.md

### Intermediate (alternative one-platform setup)

GitHub → Railway (Spring Boot \+ PostgreSQL or MySQL)

### Advanced (portfolio / learning DevOps)

GitHub → Dockerfile → Google Cloud Run or Fly.io

Database → Neon or Supabase (PostgreSQL)

### Advanced (full server control)

GitHub → Oracle Cloud Free VM

Manual: JDK 17, java \-jar, nginx reverse proxy, systemd

Database → Neon PostgreSQL or self-hosted PostgreSQL on VM

---

## 7\. Platforms to Know (not primary for this course)

| Platform | URL | Note |
| :---- | :---- | :---- |
| **Heroku** | [heroku.com](https://www.heroku.com) | Historically popular for Spring Boot; **no free dynos** since Nov 2022 — paid only |
| **Vercel / Netlify** | [vercel.com](https://vercel.com), [netlify.com](https://www.netlify.com) | Best for **frontend** static sites and serverless functions — not for long-running Spring Boot JARs |
| **GitHub Pages** | [pages.github.com](https://pages.github.com) | Static HTML/CSS/JS only — cannot run Java backend |

Use Vercel, Netlify, or GitHub Pages for your **frontend** if it is separate; host the **Spring Boot API** on Render, Railway, or another Section 2 platform.

---

## 8\. Official Documentation Links (Spring Boot \+ Cloud)

| Topic | Link |
| :---- | :---- |
| Spring Boot deployment | [docs.spring.io/spring-boot/reference/deployment](https://docs.spring.io/spring-boot/reference/deployment.html) |
| Externalized configuration | [docs.spring.io/spring-boot/reference/features/external-config](https://docs.spring.io/spring-boot/reference/features/external-config.html) |
| Spring Boot Actuator (health) | [docs.spring.io/spring-boot/reference/actuator](https://docs.spring.io/spring-boot/reference/actuator.html) |
| Render docs | [render.com/docs](https://render.com/docs) |
| Railway docs | [docs.railway.app](https://docs.railway.app) |
| Fly.io Java guide | [fly.io/docs](https://fly.io/docs/) |

---

## 9\. Related Materials in This Folder

| File | Focus |
| :---- | :---- |
| `Instruction_Using_Render.md` | Step-by-step Render deploy, GitHub upload, environment variables, databases |
| `00_Mock_Project_Instruction.md` | Mock project overview and weekly milestones |
| `01_Authentication_and_Authorization_with_Google_and_Facebook.md` | OAuth — set redirect URIs to your live `https://` URL |
| `Lectures/Spring/10_Deployment_and_Observability.md` | JAR packaging, Docker, Spring profiles, Actuator |

---

## 10\. Summary Table — Top Picks for CPL Interns

| Rank | Platform | Role | Free tier | Course guide |
| :---- | :---- | :---- | :---- | :---- |
| 1 | **Render** | App \+ PostgreSQL | Yes | `Instruction_Using_Render.md` |
| 2 | **Railway** | App \+ PostgreSQL / MySQL | Limited credit | — |
| 3 | **Neon** | PostgreSQL only | Yes | Use with any app host |
| 4 | **Fly.io** | App (Docker) | Limited free | — |
| 5 | **Google Cloud Run** | App (container) | Limited free | Advanced |

---

> **Before your presentation:** Confirm your live URL loads, database CRUD works after a redeploy, and admin/API secrets are set in the platform’s **Environment** panel — not in source code.  
