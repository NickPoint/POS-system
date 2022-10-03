### UC-2: Check history for a year

**Primary Actor:** Accountant

**Actor's goal:** To check history for all purchases that are up to one year old

**Participating actors:** system, accountant

**Precondition:** 
- System is working, it has a history view tab and history view, the history view has a button “show all” and a purchase details section. 

**Postcondition:** 
- An accountant has a list of all the purchases that are up to one year old.

**Main Success Scenario:** 

1. → Accountant chooses History view tab

2. ← System displays history view

3. → Accountant presses “Show all” button

4. ← History for a year is displayed in purchase details section

**Flow of Events for Extensions:**

 4b. POS has no transactions in memory:
  
  1. ← System shows an empty table