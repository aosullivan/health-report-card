# heath-report-card

A Clojure library to collect simple code metrics through static code analysis

## Duplication

Reports each duplicate pair

Duplicate in 2 files is 1 pair:
E:\workspace-sdlc\heath-report-card\test\java\duplication\SomeDuplication.java:6: Found duplicate of 9 lines in E:\workspace-sdlc\heath-report-card\test\java\duplication\HelloWorld.java, starting from line 12

Duplicate in 3 files is 3 pairs:
E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\SomeDuplication.java:6: Found duplicate of 9 lines in E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\HelloWorld.java, starting from line 12
E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\SomeMoreDuplication.java:6: Found duplicate of 8 lines in E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\HelloWorld.java, starting from line 12
E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\SomeMoreDuplication.java:6: Found duplicate of 8 lines in E:\workspace-sdlc\heath-report-card\test\java\duplicationx3\SomeDuplication.java, starting from line 6


## Class length
Number of lines, including whitespace, from the class declaration to the final closing brace.  

## Method length
Number of logical lines

	//ncss3 ccn1
	public int getA() {
		int temp = dodouble(a);
		return dodouble(temp);
	}

	//ncss2 ccn1
	public int getA() {
		return dodouble(dodouble(a));
	}

## Cyclomatic complexity

