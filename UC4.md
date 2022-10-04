### UC-4: Notify about needed restock

**Primary Actor:** Warehouse manager, cashier, accountant

**Actor's goal:** Receive a notification in the CLI about needed restock of sold-out and nearly sold-out products from the warehouse

**Participating actors:** system

**Precondition:** 
- The added product is nearly out of stock or out of stock

**Postcondition:** 
- Warehouse manager knows about situation and acts accordingly

**Main Success Scenario:** 

1. ← On the start-up CLI queries items that are nearly sold-out

2. ← CLI prints out a message to the user about sold-out or nearly sold-out products if there are any

3. → (a) User who first sees a pop-up notification gives this information directly to the warehouse manager
	 (b) warehouse manager themself see notification

4. → Warehouse manager finds about out of stock or nearly out of stock products

**Flow of Events for Extensions:**

1b. There are no nearly sold-out items, no action are perfomed