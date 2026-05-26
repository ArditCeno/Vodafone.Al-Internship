# TOBi2 - Vodafone Albania AI Chatbot

[![CI](https://github.com/ArditCeno/Vodafone.Al-Internship/actions/workflows/ci.yml/badge.svg)](https://github.com/ArditCeno/Vodafone.Al-Internship/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-61dafb)

TOBi2 is a bilingual (Albanian/English) AI customer service chatbot for Vodafone Albania, featuring user authentication, plan recommendations, churn prediction, voice input, photo analysis, and conversation history.

## Features

- **User authentication** — Login, registration, password change (JWT + BCrypt)
- **Conversation history** — Past chats saved per user, viewable on login
- Bilingual chat (Albanian / English)
- Account balance, billing, plan inquiries
- Smart plan recommendations based on usage
- Churn prediction dashboard (for Vodafone employees)
- Voice input (Speech-to-Text) and Text-to-Speech
- Photo upload with OCR + AI analysis
- Streaming responses (SSE)
- Human agent handoff

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Frontend | React 18, Vite 5 |
| Database | H2 (file-based, stored in `data/`) |
| Auth | JWT (jjwt) + BCrypt (spring-security-crypto) |
| Build | Maven |
| Container | Docker |
| CI/CD | GitHub Actions |

## Quick Start

### Prerequisites
- Java 17+ (project includes `jdk17.zip`)
- Node.js 20+
- Maven 3.9+

### Run Locally

```bash
# 1. Start the backend (auto-downloads Maven if missing)
run-tobi2.bat

# 2. Start the frontend (separate terminal)
run-tobi2-frontend.bat

# 3. Open http://localhost:5173
```

The backend seeds 20 users + 8 Vodafone employees automatically on first run.

## Default Users

### Regular users
Login with username + password: `{firstname}123` (e.g. `ardit123`)

| Name | Username | Password |
|------|----------|----------|
| Ardit Ceno | `arditceno` | `ardit123` |
| Jana Zyka | `janazyka` | `jana123` |
| Briselda Pasha | `briseldapasha` | `briselda123` |
| Erla Seci | `erlaseci` | `erla123` |
| Anxhela Cenaj | `anxhelacenaj` | `anxhela123` |
| Albi Ballo | `albiballo` | `albi123` |
| Gledisa Plaku | `gledisaplaku` | `gledisa123` |
| Abjola Sinanaj | `abjolasinanaj` | `abjola123` |
| Klea Ceno | `kleaceno` | `klea123` |
| Ledjon Cili | `ledjoncili` | `ledjon123` |
| Andi Vajvoda | `andivajvoda` | `andi123` |

### Vodafone Employees
Login with email + password: `{firstname}123`

| Name | Email | Password |
|------|-------|----------|
| Ardit Ceno | `ardit.ceno@vodafone` | `ardit123` |
| Jana Zyka | `jana.zyka@vodafone` | `jana123` |
| Klea Ceno | `klea.ceno@vodafone` | `klea123` |
| Ledjon Cili | `ledjon.cili@vodafone` | `ledjon123` |
| Juela Dyrimishi | `juela.dyrimishi@vodafone` | `juela123` |
| Joana Mucaj | `joana.mucaj@vodafone` | `joana123` |
| Denata Mata | `denata.mata@vodafone` | `denata123` |
| Kristiano Zyka | `kristiano.zyka@vodafone` | `kristiano123` |

All passwords are defined in `DatabaseInitializer.java` — change them in the `makePassword()` switch.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8081` | Server port |
| `TOBI2_VISION_API_KEY` | `mock` | Google Gemini API key for vision |
| `TOBI2_OCR_API_KEY` | `mock` | OCR.space API key |

## Project Structure

```
src/
├── main/java/com/vodafone/tobi2/
│   ├── agent/           # Chat agent logic
│   ├── churn/           # Churn prediction
│   ├── config/          # Spring config, DB initializer, security
│   ├── controller/      # REST API endpoints (auth, chat, conversation)
│   ├── evaluation/      # Conversation evaluation
│   ├── memory/          # Chat memory store
│   ├── model/           # Records (User, Conversation, DTOs)
│   ├── monitoring/      # Metrics
│   ├── rag/             # RAG knowledge base
│   ├── recommender/     # Plan recommendation engine
│   └── service/         # Business services (JWT, auth, conversation, etc.)
├── Tobi2/frontend/      # React frontend
└── test/                # Unit tests
```

## API Endpoints

### Chat
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tobi2/chat` | Send chat message (requires JWT) |
| GET | `/api/tobi2/chat/stream` | Streaming chat SSE (requires JWT) |
| POST | `/api/tobi2/chat/photo` | Chat with photo upload |
| POST | `/api/tobi2/stt` | Speech-to-text |
| POST | `/api/tobi2/tts` | Text-to-speech |
| POST | `/api/tobi2/rag/search` | Knowledge base search |

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tobi2/auth/login` | Login with username + password |
| POST | `/api/tobi2/auth/register` | Register new account |
| POST | `/api/tobi2/auth/change-password` | Change password (requires old password) |
| POST | `/api/tobi2/auth/admin/reset-password` | Force reset password (by username) |
| POST | `/api/tobi2/auth/hash-password` | Generate BCrypt hash for a password |
| GET | `/api/tobi2/session/start` | Start chat session |

### Conversations
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tobi2/conversations` | List user's conversations |
| GET | `/api/tobi2/conversations/{sessionId}/messages` | Get conversation messages |
| DELETE | `/api/tobi2/conversations/{sessionId}` | Archive a conversation |

### Admin
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tobi2/plan/recommend` | Plan recommendation |
| GET | `/api/tobi2/admin/churn/{userId}` | Churn score |

## Deployment

### Docker

```bash
docker build -t tobi2 .
docker run -p 8081:8081 tobi2
```

### Fly.io

```bash
fly launch
fly deploy
fly postgres create
fly postgres attach
```

## License

MIT
