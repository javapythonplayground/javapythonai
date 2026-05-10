from rag_data import knowledge_base

class AvailabilityAgent:
    def check_flight(self, source, destination):
        return {
            "available": True,
            "flight_number": "AI-202"
        }

class PricingAgent:
    def calculate_price(self):
        return 5500

class BookingAgent:
    def create_booking(self, customer_name, source, destination, flight_number):
        return {
            "status": "BOOKED",
            "customer_name": customer_name,
            "flight_number": flight_number
        }

class RAGAgent:
    def search_knowledge(self, question):
        for item in knowledge_base:
            if question.lower() in item.lower():
                return item
        return knowledge_base[0]

class RootAgent:
    def __init__(self):
        self.availability_agent = AvailabilityAgent()
        self.pricing_agent = PricingAgent()
        self.booking_agent = BookingAgent()
        self.rag_agent = RAGAgent()

    def process_booking(self, customer_name, source, destination):
        availability = self.availability_agent.check_flight(source, destination)

        if not availability["available"]:
            return {"status": "FAILED"}

        price = self.pricing_agent.calculate_price()

        booking = self.booking_agent.create_booking(
            customer_name,
            source,
            destination,
            availability["flight_number"]
        )

        booking["price"] = price
        return booking