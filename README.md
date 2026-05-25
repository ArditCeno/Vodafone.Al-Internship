# TOBi2 - Vodafone Albania AI Chatbot

[![CI](https://github.com/ArditCeno/Vodafone.Al-Internship/actions/workflows/ci.yml/badge.svg)](https://github.com/ArditCeno/Vodafone.Al-Internship/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-61dafb)

TOBi2 is a bilingual (Albanian/English) AI customer service chatbot for Vodafone Albania, featuring plan recommendations, churn prediction, voice input, and photo analysis.

## Features

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
| Database | H2 (file-based) |
| Build | Maven |
| Container | Docker |
| CI/CD | GitHub Actions |

## Quick Start

### Prerequisites
- Java 17+
- Node.js 20+
- Maven 3.9+

### Run Locally

```bash
# 1. Start the backend
run-tobi2.bat

# 2. Start the frontend (separate terminal)
run-tobi2-frontend.bat

# 3. Open http://localhost:5173
```

### Docker

```bash
docker build -t tobi2 .
docker run -p 8081:8081 tobi2
```

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
│   ├── config/          # Spring config (CORS, etc.)
│   ├── controller/      # REST API endpoints
│   ├── evaluation/      # Conversation evaluation
│   ├── memory/          # Chat memory store
│   ├── monitoring/      # Metrics
│   ├── rag/             # RAG knowledge base
│   ├── recommender/     # Plan recommendation engine
│   └── service/         # Business services (STT, TTS, OCR, etc.)
├── Tobi2/frontend/      # React frontend
└── test/                # Unit tests
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tobi2/chat` | Send chat message |
| GET | `/api/tobi2/chat/stream` | Streaming chat (SSE) |
| POST | `/api/tobi2/chat/photo` | Chat with photo upload |
| POST | `/api/tobi2/stt` | Speech-to-text |
| POST | `/api/tobi2/tts` | Text-to-speech |
| POST | `/api/tobi2/rag/search` | Knowledge base search |
| POST | `/api/tobi2/auth/login` | User login |
| GET | `/api/tobi2/session/start` | Start chat session |
| GET | `/api/tobi2/plan/recommend` | Plan recommendation |
| GET | `/api/tobi2/admin/churn/{userId}` | Churn score |

## Deployment

[![Fly.io](https://img.shields.io/badge/Fly.io-24175C?logo=fly)](https://fly.io)

```bash
fly launch
fly deploy
fly postgres create
fly postgres attach
```

## License

MIT
