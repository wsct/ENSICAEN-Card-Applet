# ISO/IEC 7816 Smartcard Applet

## Objectives

Create a new smartcard to be used at [ENSICAEN](https://www.ensicaen.fr/) during some practical exercices.

-|Supported version
---|---
Java Card | 2.2.1+
Transmission protocols | T=0 & T=1 
IDE used | [JCIDE](https://www.javacardos.com/tools/) and IntelliJ IDEA
CAP Build | JCIDE or Apache ant

The APDU are inspired by the TB100 (T=0) smartcard we were using for 15 years.

## Supported APDU
  * SELECT
  * CREATE FILE
  * DELETE FILE
  * READ BINARY
  * WRITE BINARY
  * ERASE

## Notes about unit tests

Some unit tests (written and run under IntelliJ IDEA) relying on the Java Card API *may not be accurate*:
Java Card Virtual Machine is hosted on a smartcard but these unit tests are based on a fake Java Card API.
They only allow some regression checks.