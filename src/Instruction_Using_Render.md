# Instruction: Using Render to Deploy Your Project

**Program:** FSA CPL Internship — Java Fullstack  
**Purpose:** Deploy your Mock Project or CPL Project to a public URL so trainers, teammates, and reviewers can access it online  
**Audience:** Any intern or team deploying a Spring Boot (or similar) application — not tied to a specific sample project

---

## 1\. What Is Render?

**Render** ([https://render.com](https://render.com)) is a **cloud hosting platform** (PaaS — Platform as a Service). It builds and runs your application from GitHub so you do not need to manage your own server.

| Concept | Meaning |
| :---- | :---- |
| **Web Service** | Your running application (e.g. Spring Boot on port 8080\) |
| **Database** | A managed database hosted by Render (or an external provider) |
| **Blueprint** | One-click deploy from a `render.yaml` file in your Git repository |
| **Environment Variables** | Config and secrets (DB URL, API keys, passwords) set in the Render dashboard — never committed to Git |
| **Deploy** | Render pulls code from GitHub, builds it, and starts the app |

### Why use Render for your project?

- Share a **live demo URL** (e.g. `https://your-app.onrender.com`) in presentations and sprint reviews  
- Practice a **real deployment workflow**: Git → build → run → configure environment  
- Free tier available for learning (with limits — see Section 9\)

### Typical files you prepare in your own repository

| File | Role |
| :---- | :---- |
| `Dockerfile` | Builds your app inside a Docker image (recommended for Spring Boot) |
| `render.yaml` | Optional Blueprint — defines web service, database, and env vars in one file |
| `application-prod.properties` | Production Spring profile — reads `PORT`, database URL, and secrets from environment variables |
| Health endpoint (e.g. `/health` or `/actuator/health`) | Lets Render verify your app is running |

---

## 2\. Databases You Can Use on Render

Your app needs a **persistent database** in production. Data stored only in memory or in a local H2 file will be **lost** when Render restarts or redeploys your service.

### 2.1 Render-managed databases (recommended)

Render provides these managed databases directly in the dashboard:

| Database | Best for | Free tier | Spring Boot support |
| :---- | :---- | :---- | :---- |
| **PostgreSQL** | Most Spring Boot \+ JPA projects | Yes | **Recommended** — native JDBC driver included |
| **Redis** | Caching, sessions, rate limiting | Yes | Use with `spring-boot-starter-data-redis` (optional add-on) |

> **PostgreSQL is the default choice** for Mock and CPL projects. It is fully supported on Render's free tier and works well with Spring Data JPA.

#### Create a PostgreSQL database on Render

| Step | Action |
| :---- | :---- |
| 1 | Render Dashboard → **New \+** → **PostgreSQL** |
| 2 | **Name:** e.g. `my-project-db` |
| 3 | **Database:** e.g. `my_project_db` |
| 4 | **User:** auto-generated (e.g. `my_project_user`) |
| 5 | **Region:** same region as your Web Service (lower latency) |
| 6 | **Plan:** Free (for learning and demos) |
| 7 | Click **Create Database** |

After creation, open the database → **Connections** tab. Copy:

- **Internal Database URL** — use this when your Web Service is also on Render (faster, private network)  
- **External Database URL** — use only if connecting from outside Render (e.g. your laptop or another cloud)

#### Spring Boot settings for Render PostgreSQL

Add these **Environment variables** on your Web Service:

| Key | Value |
| :---- | :---- |
| `SPRING_DATASOURCE_URL` | Internal Database URL from Render (starts with `postgresql://...`) |
| `SPRING_DATASOURCE_USERNAME` | Database user from Render |
| `SPRING_DATASOURCE_PASSWORD` | Database password from Render |
| `SPRING_DATASOURCE_DRIVER` | `org.postgresql.Driver` |
| `SPRING_JPA_DATABASE_PLATFORM` | `org.hibernate.dialect.PostgreSQLDialect` |

**Maven dependency** (add to `pom.xml` if not already present):

\<dependency\>

    \<groupId\>org.postgresql\</groupId\>

    \<artifactId\>postgresql\</artifactId\>

    \<scope\>runtime\</scope\>

\</dependency\>

**Example `application-prod.properties`:**

spring.datasource.url=${SPRING\_DATASOURCE\_URL}

spring.datasource.username=${SPRING\_DATASOURCE\_USERNAME}

spring.datasource.password=${SPRING\_DATASOURCE\_PASSWORD}

spring.datasource.driver-class-name=${SPRING\_DATASOURCE\_DRIVER:org.postgresql.Driver}

spring.jpa.database-platform=${SPRING\_JPA\_DATABASE\_PLATFORM:org.hibernate.dialect.PostgreSQLDialect}

spring.jpa.hibernate.ddl-auto=update

---

### 2.2 If your project uses MySQL locally

Render does **not** offer managed MySQL. You have two practical options:

| Option | Approach |
| :---- | :---- |
| **A — Switch to PostgreSQL for production** | Easiest for Render. Keep MySQL locally; use PostgreSQL on Render with the same JPA entities. |
| **B — Use an external MySQL host** | Keep MySQL in production via a third-party provider (see table below). |

#### External database providers (when not using Render PostgreSQL)

| Provider | Databases | Free tier | Notes |
| :---- | :---- | :---- | :---- |
| [Neon](https://neon.tech) | PostgreSQL | Yes | Serverless PostgreSQL; works well with Render |
| [Supabase](https://supabase.com) | PostgreSQL | Yes | PostgreSQL \+ optional extras |
| [PlanetScale](https://planetscale.com) | MySQL | Limited free | Good if you must keep MySQL |
| [Railway](https://railway.app) | PostgreSQL, MySQL, Redis | Limited free | Separate platform; can pair with Render web service |
| [Aiven](https://aiven.io) | PostgreSQL, MySQL, Redis | Trial / paid | Enterprise-grade option |

For external databases, copy the **JDBC connection string** from the provider and set `SPRING_DATASOURCE_URL`, `USERNAME`, and `PASSWORD` in Render Environment — same process as Section 6\.

**MySQL driver dependency:**

\<dependency\>

    \<groupId\>com.mysql\</groupId\>

    \<artifactId\>mysql-connector-j\</artifactId\>

    \<scope\>runtime\</scope\>

\</dependency\>

**MySQL environment example:**

| Key | Value |
| :---- | :---- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://HOST:3306/DATABASE?serverTimezone=UTC` |
| `SPRING_DATASOURCE_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `SPRING_JPA_DATABASE_PLATFORM` | `org.hibernate.dialect.MySQLDialect` |

---

### 2.3 Redis (optional — not a primary database)

Use Redis on Render when your project needs caching or session storage — not as your main data store.

| Step | Action |
| :---- | :---- |
| 1 | **New \+** → **Redis** |
| 2 | Name it (e.g. `my-project-cache`) |
| 3 | Copy the **Internal Redis URL** |
| 4 | Set env var e.g. `SPRING_DATA_REDIS_URL` on your Web Service |

---

### 2.4 Database choice summary

| Your local setup | Recommended on Render |
| :---- | :---- |
| PostgreSQL | Render PostgreSQL |
| MySQL | Switch to Render PostgreSQL **or** external MySQL (PlanetScale, Railway) |
| H2 (in-memory / file) | **Must replace** with Render PostgreSQL for production |
| No database yet | Create Render PostgreSQL before deploying |

---

## 3\. Prerequisites

Before deploying to Render:

1. **GitHub account** — [https://github.com](https://github.com)  
2. **Project pushed to GitHub** — see Section 4  
3. **App runs locally** without errors (`./mvnw spring-boot:run` or equivalent)  
4. **Production profile** — e.g. `application-prod.properties` with `${ENV_VAR}` placeholders  
5. **Dockerfile** (recommended) or a defined build/start command for Render  
6. **Database created** — Render PostgreSQL (Section 2.1) or external provider (Section 2.2)

---

## 4\. Upload Code to GitHub (Required Before Render)

Render deploys from **GitHub**, not directly from your laptop.

### Step 4.1 — Create a GitHub repository

1. Sign in to GitHub → **New repository**  
2. Name it (e.g. `warehouse-mock-app`, `clinic-booking-system`)  
3. Choose **Public** or **Private** (Render supports both if access is granted)  
4. Do **not** add a README if you already have a local project  
5. Click **Create repository**

### Step 4.2 — Push your local project

Open a terminal in your **project root**:

git init

git add .

git commit \-m "Initial commit"

git branch \-M main

git remote add origin https://github.com/YOUR\_USERNAME/YOUR\_REPO.git

git push \-u origin main

Replace `YOUR_USERNAME` and `YOUR_REPO` with your details.

### Step 4.3 — Verify on GitHub

Confirm your repository contains at minimum:

- `pom.xml` (or `build.gradle`)  
- Source code (`src/main/java/...`)  
- `Dockerfile` (if using Docker deploy)  
- `render.yaml` (optional — for Blueprint deploy)

>   
> **Important:** Never commit real passwords, API keys, or `application-local.properties`. Add secrets only in Render **Environment** (Section 7).

---

## 5\. Create a Render Account and Connect GitHub

### Step 5.1 — Sign up

1. Go to [https://render.com](https://render.com)  
2. Click **Get Started** or **Sign Up**  
3. Choose **Sign in with GitHub** (recommended)  
4. Authorize Render to access your GitHub account

### Step 5.2 — Grant repository access

For **private** repositories:

1. Render → **Account Settings** → **GitHub** → **Configure account**  
2. Select **Only select repositories** and add your project repo  
3. Save

**Public** repositories are accessible after GitHub sign-in.

---

## 6\. Deploy on Render — Step by Step

Two methods: **Blueprint** (if you have `render.yaml`) or **Manual** (full control).

---

### Method A — Blueprint deploy (when `render.yaml` exists)

A Blueprint reads `render.yaml` from your repo and creates all resources (web service, database, env vars) in one step.

| Step | Action |
| :---- | :---- |
| 1 | Render Dashboard → **New \+** → **Blueprint** |
| 2 | Connect your GitHub repository |
| 3 | Render shows planned resources (web service \+ database) from `render.yaml` |
| 4 | Review settings → click **Apply** |
| 5 | Wait for **Build** then **Deploy** (first build: 5–15 minutes) |
| 6 | Open your live URL: `https://YOUR-SERVICE-NAME.onrender.com` |

**Example `render.yaml` snippet** (adjust names to your project):

services:

  \- type: web

    name: my-spring-app

    runtime: docker

    dockerfilePath: ./Dockerfile

    plan: free

    healthCheckPath: /health

    envVars:

      \- key: SPRING\_PROFILES\_ACTIVE

        value: prod

      \- key: SPRING\_DATASOURCE\_URL

        fromDatabase:

          name: my-project-db

          property: connectionString

      \- key: SPRING\_DATASOURCE\_USERNAME

        fromDatabase:

          name: my-project-db

          property: user

      \- key: SPRING\_DATASOURCE\_PASSWORD

        fromDatabase:

          name: my-project-db

          property: password

databases:

  \- name: my-project-db

    plan: free

    databaseName: my\_project\_db

    user: my\_project\_user

---

### Method B — Manual deploy (most common for custom projects)

#### Step B.1 — Create the database first

Follow **Section 2.1** to create a Render PostgreSQL database. Keep the **Connections** tab open.

#### Step B.2 — Create the Web Service

1. **New \+** → **Web Service**  
2. Connect your GitHub repository  
3. Configure:

| Setting | Typical value |
| :---- | :---- |
| **Name** | `my-spring-app` (becomes part of your URL) |
| **Region** | Same as your database |
| **Branch** | `main` |
| **Root Directory** | Leave blank, or subfolder if your app is nested (e.g. `backend`) |
| **Runtime** | **Docker** (if you have a `Dockerfile`) or **Native** |
| **Dockerfile Path** | `./Dockerfile` |
| **Instance Type** | Free |

4. **Advanced settings:**  
   - **Health Check Path:** `/health` or `/actuator/health`  
   - **Health Check Grace Period:** `300` seconds (helps free-tier cold starts)

#### Step B.3 — Build and start commands

**If using Docker** — Render builds from your `Dockerfile` automatically. Example `Dockerfile`:

FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw pom.xml ./

COPY .mvn .mvn

RUN chmod \+x mvnw && ./mvnw dependency:go-offline \-B

COPY src ./src

RUN ./mvnw package \-DskipTests \-B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY \--from=build /app/target/\*.jar app.jar

ENV SPRING\_PROFILES\_ACTIVE=prod

EXPOSE 8080

CMD \["sh", "-c", "java \-jar app.jar \--server.port=${PORT:-8080}"\]

**If using Native runtime** (no Docker):

| Field | Value |
| :---- | :---- |
| **Build Command** | `./mvnw clean package -DskipTests` |
| **Start Command** | `java -jar target/YOUR-APP-*.jar --spring.profiles.active=prod` |

5. Click **Create Web Service**

#### Step B.4 — Link the database to the Web Service

1. Open your Web Service → **Environment**  
2. Add database variables from Section 2.1 (or use **Add from Render PostgreSQL** if shown)  
3. **Save Changes** — Render redeploys automatically

---

## 7\. Environment Settings

Render passes configuration to your app as **environment variables**. Spring Boot reads them via `${VAR_NAME}` in `application-prod.properties`.

### Where to set variables

1. Open your **Web Service** in the Render dashboard  
2. Go to **Environment**  
3. Add or edit **Key / Value** pairs  
4. Click **Save Changes** → Render redeploys

### Core variables (every Spring Boot deploy)

| Key | Purpose | Example |
| :---- | :---- | :---- |
| `SPRING_PROFILES_ACTIVE` | Activates production profile | `prod` |
| `PORT` | HTTP port (set by Render automatically) | Do not override |
| `SPRING_DATASOURCE_URL` | Database JDBC URL | From Render PostgreSQL → Connections |
| `SPRING_DATASOURCE_USERNAME` | DB username | From Render PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | DB password | From Render PostgreSQL |
| `SPRING_DATASOURCE_DRIVER` | JDBC driver | `org.postgresql.Driver` |
| `SPRING_JPA_DATABASE_PLATFORM` | Hibernate dialect | `org.hibernate.dialect.PostgreSQLDialect` |
| `JAVA_TOOL_OPTIONS` | JVM memory limit (free tier) | `-Xmx384m -XX:+UseSerialGC` |

### Application / admin variables (if your app has admin login)

Use your own values — these are **examples only**:

| Key | Purpose | Example value |
| :---- | :---- | :---- |
| `ADMIN_USERNAME` | Admin login username | `project_admin` |
| `ADMIN_EMAIL` | Admin account email | `admin@yourteam.com` |
| `ADMIN_PASSWORD` | Admin password | Set a strong password in Render |
| `ADMIN_FIRST_NAME` | Display name | `Project` |
| `ADMIN_LAST_NAME` | Display name | `Admin` |

> Change `ADMIN_PASSWORD` to a strong value in Render. Do not use `1234`, `admin`, or any default from sample code.

### Optional — third-party API keys

| Key | Purpose |
| :---- | :---- |
| `OPENAI_API_KEY` | AI / chatbot features |
| `GEMINI_API_KEY` | Backup AI provider |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth login |
| `FACEBOOK_APP_ID` / `FACEBOOK_APP_SECRET` | Facebook OAuth login |

### Optional — public URL and analytics

| Key | Purpose |
| :---- | :---- |
| `APP_PUBLIC_URL` | Your live base URL (e.g. `https://my-spring-app.onrender.com`) |
| `GA4_MEASUREMENT_ID` | Google Analytics tracking ID |

### Local vs Render — where secrets live

| Environment | Where to put secrets |
| :---- | :---- |
| **Local development** | `application-local.properties` or `.env` (must be in `.gitignore`) |
| **Render production** | Render Dashboard → **Environment** only |

Never commit API keys, database passwords, or admin credentials to GitHub.

---

## 8\. After Deploy — Verify and Share

### Checklist

| \# | Check | How |
| :---- | :---- | :---- |
| 1 | Service is **Live** | Render dashboard shows green **Live** status |
| 2 | Health endpoint works | `https://YOUR-APP.onrender.com/health` returns OK |
| 3 | Home page loads | Open your app root URL |
| 4 | Database connected | Create or read a record; check **Logs** for SQL errors |
| 5 | Admin login works | Use the `ADMIN_USERNAME` and `ADMIN_PASSWORD` you set in Environment |

### View logs

Web Service → **Logs** — use for build failures, memory errors, and database connection issues.

### Auto-deploy on Git push

Pushing to your connected branch (usually `main`) triggers a new deploy:

git add .

git commit \-m "Add order status feature"

git push

---

## 9\. Free Tier — What to Expect

| Topic | Behavior |
| :---- | :---- |
| **Cold start** | Free web services spin down after \~15 min idle; first visit can take 30–60+ seconds |
| **Memory** | \~512 MB RAM — set `JAVA_TOOL_OPTIONS=-Xmx384m` |
| **PostgreSQL** | Free tier has storage and connection limits; sufficient for demos and Mock projects |
| **Health check** | Use a lightweight path like `/health`; avoid `/` alone on cold start |
| **URL** | Free `*.onrender.com` subdomain is enough for presentations |

---

## 10\. Troubleshooting

| Problem | Likely cause | Fix |
| :---- | :---- | :---- |
| Build failed | Maven/Docker error | Read **Logs**; fix `pom.xml`, `Dockerfile`, or wrong root directory |
| App failed to respond | Wrong port or address | Listen on `0.0.0.0` and use `${PORT}` in `application-prod.properties` |
| Database connection error | Wrong URL, user, or driver | Verify env vars; use Internal URL; check PostgreSQL driver in `pom.xml` |
| SSL / connection refused | External URL used incorrectly | Use **Internal** Database URL between Render services |
| 502 / timeout on first load | Cold start | Wait and refresh; set health check grace period to 300s |
| OutOfMemoryError | Heap too large for free tier | `JAVA_TOOL_OPTIONS=-Xmx384m -XX:+UseSerialGC` |
| Admin login fails | Wrong or unset password | Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` in Environment |
| Data lost after redeploy | Using H2 or in-memory DB | Switch to Render PostgreSQL (Section 2\) |
| Secrets exposed on GitHub | Committed `.env` or properties file | Rotate all keys; use Render Environment only |

---

## 11\. Quick Reference — Full Workflow

1\. Build and test your app locally

2\. Create Render PostgreSQL database (Section 2.1)

3\. git push your project to GitHub

4\. Render → Sign in with GitHub

5\. New → Web Service (or Blueprint if you have render.yaml)

6\. Set Environment variables: DB connection \+ ADMIN\_PASSWORD \+ API keys

7\. Deploy → wait for Live status

8\. Test /health, CRUD, and login

9\. Share https://YOUR-APP.onrender.com in your presentation

---

## 12\. Related Materials

| Resource | Location |
| :---- | :---- |
| Mock project overview | `00_Mock_Project_Instruction.md` |
| Deployment concepts (JAR, Docker, profiles) | `Lectures/Spring/10_Deployment_and_Observability.md` |
| GitHub upload basics | `Lectures/VCSA/FU-01/06.GitHub_Portfolio_Upload_Guide.md` |
| OAuth setup | `01_Authentication_and_Authorization_with_Google_and_Facebook.md` |

---

> **Presentation tip:** Show both your **GitHub repository URL** and your **Render live URL**. Explain which database you chose (Render PostgreSQL or external) and at least one environment variable you configured — this demonstrates production deployment awareness.  
