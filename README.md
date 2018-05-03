## Java Static Analyser
A tiny static analyser for java projects. Implemented analyses:
1. Search for always false and always true boolean subexpressions
2. Search for suspicious usage of shifts (<<, >>, >>>), possible error with operator precedence
3. Search for identical then-else branches

### Build
The simplest way to build this project is to create a IntelliJ IDEA project from existing sources:
1. Open Intellij IDEA
2. File > New > Project from existing sources...
3. Pick a root of this repository
4. In the following dialogs add `src/` as a source root and `lib/javaparser-core-3.6.2.jar` as the only library
5. Create run configuration with `MainKt` as a main class
6. Build / run this configuration

### Run
If you have an already packed jar or just a run configuration, you can run the analyser by passing the only command line argument, path to the project to analyse.

Example:
```
java -jar JavaStaticAnalyser.jar C:\path\to\my\project
```  
The results of the analyser will be stored in the `warnings.log` file.
Example of this file for `jmonkeyengine` project:
```
File: C:\Users\user\jmonkeyengine\jme3-ios\src\main\java\com\jme3\system\ios\IosLogHandler.java
At [(line 55,col 14)-(line 60,col 9)]: Branches of 'if' statement are equivalent

File: C:\Users\user\jmonkeyengine\jme3-networking\src\main\java\com\jme3\network\base\DefaultServer.java
At [(line 477,col 9)-(line 481,col 9)]: Branches of 'if' statement are equivalent

File: C:\Users\user\jmonkeyengine\jme3-vr\src\main\java\com\jme3\input\vr\AbstractVRMouseManager.java
At [(line 116,col 8)-(line 132,col 14)]: Branches of 'if' statement are equivalent

File: C:\Users\user\jmonkeyengine\jme3-vr\src\main\java\com\jme3\system\jopenvr\OpenVRUtil.java
At [(line 62,col 10)-(line 66,col 3)]: Branches of 'if' statement are equivalent
```