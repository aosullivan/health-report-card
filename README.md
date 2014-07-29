# heath-report-card

A Clojure library to collect simple code metrics through static code analysis.

java -cp target\heath-report-card-0.2.0-SNAPSHOT-standalone.jar  health_report_card.core  <src>

## Duplication
Duplication is detected by simple textual comparison.

## Class Length
Number of lines, including all whitespace and comments, from the class declaration to the final closing brace.  

## Average Method Length
Length is measured in terms of non-commenting source statments (NCSS). NCSS is roughly equivalent to counting ';' and '{' characters in Java source files.

http://www.kclee.de/clemens/java/javancss/#specification

Example:

	//ncss3 
	public int getA() {
		int temp = dodouble(a);
		return dodouble(temp);
	}

	//ncss2 
	public int getA() {
		return dodouble(dodouble(a));
	}


## Cyclomatic Complexity Number (CCN)
CCN is also know as McCabe Metric. Each method has a minimum value of 1 per default. Whenever the control flow of a method splits, the CCN number gets incremented:

if
for
while
case
catch
&&
||
?

Note that else, default, and finally don't increment the CCN value any further. On the other hand, a simple method with a switch statement and a huge block of case statements can have a surprisingly high CCN value (still it has the same value when converting a switch block to an equivalent sequence of if statements). 

## Method length and CCN excluding one line methods
We also report the average values of method length and CCN when all one-line methods are excluded. More precisely, we exclude methods where NCSS <= 2. This is in order to discount classes like Javabeans and DTO's which have a high proportion of methods with no logic.

Interface method declarations have an NCSS of 1 and a CCN of 1.  These are included in the averages, but excluded from one-liner averages.