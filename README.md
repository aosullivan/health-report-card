# health-report-card

A Clojure library to collect simple code metrics through static code analysis.

### To run:

java -jar <jar>.jar  [-d --debug] <Java source folder>

### To build

Ensure leiningen is installed: http://leiningen.org/#install
lein uberjar


Add in best practices

I THINK methods should be an OR, i.e. violations OR - difficult cos one of the results is in PMD. If we move all to PMD, maybe doable


http://www.javacodegeeks.com/2012/12/rule-of-30-when-is-a-method-class-or-subsystem-too-big.html

## Duplicated lines
Duplication is detected by simple textual comparison of tokens using PMD/CPD.

## Methods with \# statements > 30
Number of methods with more than 30 statements

Statements are roughly equivalent to counting ';' and '{' characters in Java source files.

Statements counted by NCSS: http://javancss.codehaus.org/specification.html

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


## Methods with Cyclomatic Complexity > 10
CCN is also know as McCabe Metric. Each method has a minimum value of 1 per default. Whenever the control flow of a method splits, the CCN number gets incremented:

if  \n
for \n
while \n
case \n
catch \n
&& \n
|| \n
? \n

Note that else, default, and finally don't increment the CCN value any further. On the other hand, a simple method with a switch statement and a huge block of case statements can have a surprisingly high CCN value (still it has the same value when converting a switch block to an equivalent sequence of if statements). 

## Methods > 3 Parameters

Self explanatory

## Packages with classes > 25

## Classes with statements > 300

