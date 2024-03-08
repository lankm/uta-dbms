# Project Two Documentation

## Topics
 - Transaction managment
 - Concurrency control and recovery

<!--
This is just a basic documentation area for this part of the project. We can upload images to the folder and link it here.

The language for this assignment is C++. The skeleton code given by the professor should be C++
-->

## Notes (Delete when done)
- Write an error to the log if a thread fails to be created
- "Since each operation of each transaction is done in a separate thread, all errors and the log output need to be printed (or transmitted) at the point of occurrence. These threads cannot return any error status"
- Don't add new functions?  "All the functions you need to implement are specified in the files. If you feel you need to add new functions, please discuss that with the instructor or the TA so that we can understand their need and help you implement it correctly!"

- semno = semaphore number (default: -1)
- lock = lock mode (default: "")
- tx_type = transaction type (Default read from file)
- objno = object number of the object you are waiting on
- Txstatus = status of transation
- Thrid = thread rid
- Tid = Transaction id?

- Manually fix semaphores after crash/termination (ipcs_cleanup.sh)
- sem<SHARED_MEM_AVAIL> used for accessing the hash map

### Code Readthrough Notes
- The 'optime' (operation time) is randomized for each operation. Array of optimes are defined in the constructor for zgt_tm()

### CMD quick access
- sample cmd exec:
    - make clean
    - make
    - ./zgt_test ..test-files/deadlock.txt
- clear semaphores:
    - sh ipcs_cleanup.sh
    - ./ipcs_cleanup.sh
- see system semaphores
    - ipcs
