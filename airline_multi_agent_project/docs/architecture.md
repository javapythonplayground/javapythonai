# Architecture

Customer UI
   |
React Frontend
   |
FastAPI Root Agent
   |
------------------------------------------------
|            |             |                   |
Availability Pricing      Booking            RAG
Agent        Agent        Agent              Agent
   |
PostgreSQL Database

## RAG Flow

1. User asks policy question
2. RAG agent searches knowledge base
3. Relevant answer returned