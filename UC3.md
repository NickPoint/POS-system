### UC-3: Suggest product name

**Primary Actor:** Warehouse manager

**Actor's goal:** To choose a product name from name suggestions which appear if there are existing products in the warehouse that have name matching already typed part of the product name

**Participating actors:** system, warehouse manager, cashier

**Precondition:** 
- System is working, it has a warehouse view tab, warehouse view displays a form to set up an item with a text field for a product name.

**Postcondition:** 
- Product form is filled

**Main Success Scenario:** 

1. → Warehouse manager chooses Warehouse view tab

2. ← System displays warehouse view

3. → Warehouse manager starts filling product name field in the form

4. ← Products with names matching already typed part of the name appear as suggestions

5. → Manager chooses the needed product

6. ← Barcode, product price and name is filled automatically, based on the already existing information in the database

**Flow of Events for Extensions:**

 5b. Warehouse does not have product in the list(the product is new):
  
  1. → Manager fills up the name field manually 