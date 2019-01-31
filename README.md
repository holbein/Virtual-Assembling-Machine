# Virtual-Assembling-Machine (VAM)
This is a virtual machine you can program in assembler.
It is easy to use and perfect for beginners!

The commands:
- LOAD x:
  - copies the value in Rx to A 
  - increases the value in BZ by 1
- DLOAD i:
  - directly loads the number i in A
  - increases the value in BZ by 1
- STORE x:
  - copies the value in A to Rx
  - increases the value in BZ by 1
- ADD x:
  - adds the value in Rx to the value of A
  - stores the result in A
  - increases the value in BZ by 1
- SUB x:
  - subtracts the value in Rx from the value in A
  - stores the result in A
  - increases the value in BZ by 1
- MULT x:
  - multiplies the value in Rx with the value in A
  - stores the result in A
  - increases the value in BZ by 1
- DIV x:
  - devides the value in A by the value in Rx
  - stores the result in A
  - increases the value in BZ by 1
- JUMP n:
    - loads the number n in BZ
- JGE n:
    - loads the number n in BZ if the value in A is bigger or equal zero
    - if not: increases the value in BZ by 1
- JGT n:
    - loads the number n in BZ if the value in A is greater than zero
    - if not: increases the value in BZ by 1
- JLE n:
    - loads the number n in BZ if the value in A is less or equal zero
    - if not: increases the value in BZ by 1
- JLT n:
    - loads the number n in BZ if the value in A is less than zero
    - if not: increases the value in BZ by 1
- JEQ n:
    - loads the number n in BZ if the value in A is equal zero
    - if not: increases the value in BZ by 1
- JNE n:
    - loads the number n in BZ if the value in A is not equal zero
    - if not: increases the value in BZ by 1
- END:
    - increases the value in BZ by 1
    - terminates the program run
    
   
Other important stuff:
  - Rx:
    - a register you can use to save numbers (1 Byte)
    - this virtual machine has got 15 (x can be an number from 1 to 15)
  - A:
    - the accumulator contains the value for the next calculation and gets its result
  - BZ:
    - contains the address of the next command (the line number)
    - if you change this number, you can jump to an other part of your code
  - SR: 
    - the Status Register contains the flags whether the result of the last logical operation was too big for one byte (Overflow) an if it was      bigger or smaller than zero
    
    
    PS: The icons are from http://famfamfam.com/ !
