<!--
More information found on page 9 of project_description.pdf. https://github.com/lankm/uta-dbms/issues/5

Convert to pdf when finalized.
-->

# Project 1 Report

## Overall Status

### Current status: Completed

### Implemented Correctly

Insert(), _insert(), and NaiveDelete() are believed to be working properly

### Unfinished Work

None.

## File Descriptions

No new files were created. Only changes are in the designated area in BTreeFile.java.

## Division of Labor

Initially we divided the labor by getting together and completing the required functions in the order which they are needed. This is Insert(), _insert(), then NaiveDelete(). This work was done in a pair programming fasion so many logical errors could be avoided on the first pass.

After the second coding session, Jacob decided to complete the rest of _insert() and NaiveDelete(). This code was then tested by both members to confirm that it works like it should. More in depth testing was still needed.

 - Landon Moon
    - 2 hours - 2/1/24  - Reading docs and started on Insert()
    - 2 hours - 2/8/24  - Finished Insert(). Helped polish and bugfix _insert()
    - 1 hour  - 2/11/24 - Report writing
 - Jacob Holz
    - 2 hours - 2/1/24  - Reading docs and started on _insert()
    - 2 hours - 2/8/24  - Completed rough draft of _insert() index page traversal/splitting
    - 2 hours - 2/9/24  - Completed rough draft of _insert() leaf insertion/splitting
    - 2 hours - 2/10/24 - Completed _insert and rough draft of NaiveDelete()
    - 2 hours - 2/11/24 - Complete NaiveDelete and test all methods
    - 1 hour  - 2/12/24 - Report writing


## Logical errors and how you handled them
Some logical errors were avoided before they cropped up through the use of pair programming, and reviewing line by line.

 - Logical Error 1: index page splitting

   After looking through the provided library, we found out that there was no getCurent(rid) function in BTIndexPage. Both the getFirst(rid) and getNext(rid) are intended to be used as iterators but because we are changing the underlying data, this would be ill-advised. When splitting an index page, records from the first page needs to be removed and inserted into the second page. Without in-depth knowlege of the functions above, an iterator would give unusual results when deleting parts of the original array. This logical error was avoided by using getNext(rid) as an accessor by setting rid to the previous slotNo. This gave us more control over which rid we were copying and deleting

   There is probably a way to use getRecord(rid) instead of our implementation, but converting a custom Tuple class to a KeyDataEntry class felt overboard.

 - Logical Error 2: Off by 1 error in above implementation

   When using getNext in this way, we overcompensated for the fact that it gets the next value by one each time it was used to get the middle value. This resulted in one entry being left over when entries were moved over resulting in invalid B+ trees upon testing.

   Solved easily by increasing the preMiddleSlotNumber by 1 and renaming it middleSlotNumber. This resulted in moving the window to be moved 1 index over which is correct.

 - Logical Error 3: NaiveDelete only deleting duplicates in the same page
   
   When there were multiple pages containing the same value, only the last page would have the entries deleted. This was fixed by interating back through the pages until there stopped being matching entries. This case only occures when there are many many duplicates of the exact same entry.