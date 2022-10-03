###UC-1: Add a product to the warehouse

**Primary Actor:** Warehouse manager

**Actor's goal:** To add a new product (there exists no entry for this product in the warehouse) to the warehouse through GUI in the warehouse view.

**Participating actors:** system, warehose manager, cashier, scanner

**Precondition:** 
- System is working, it has a warehouse view tab, warehouse view displays a form to set up an item and a button “Add product” to add it to the warehouse. 

**Postcondition:** 
- New product is added to the warehouse.

**Main Success Scenario:** 

1. → Warehouse manager chooses Warehouse view tab

2. ← System displays warehouse view

3. → Warehouse manager fills up the form

4. → Warehouse manager presses the “Add product” button to  add a new product

5. ← Form is cleared up

**Flow of Events for Extensions:**

3. b Form is filled incorrectly
  
  1. ← System (a) detects error and (b) notifies the actor about incorrectly filled fields 
  
  2. → Warehouse manager provides correct data to the form
  
  3. → Sames as in Steps 4 and 5 above.
