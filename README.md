# Imbot &nbsp; ![DEVELOPMENT STATUS: version 0.1](https://badgen.net/badge/DEVELOPMENT%20STATUS/version%200.1/green)

**Im**proved Ro**bot**
<br><br>
An improved version of the Java [Robot](https://docs.oracle.com/javase/7/docs/api/java/awt/Robot.html), with tones of additional features and functionalities.

## Features:
Functionalities are regrouped inside static classes, each for a specific group of tasks. These groups / categories and some of their functionalities are:
- **Mouse** (`mse`): Move, click, and drag. Both human-like and "instant".
- **Keyboard** (`kyb`): Type, press, and release.
- **Screen** (`scr`): Find / search for images, or take screen captures.
- **Images and Colors** (`img`): Utilities related to images and colors.
- **Clipboard** (`cp`): Copying and pasting text.
- **Files** (`file`): Reading and writing to files.
- **Utilities** (`util`): Miscellaneous utilities.

Another feature of `Imbot` is its ability to detect when the user wants to interrupt and end the program. This is useful for example when making a bot for a game, and it starts doing something it's not supposed to do. The user can simply interrupt the program by jittering the mouse around to stop the program. A Runnable can also be defined to run when the program is interrupted; in case the user wants, for example, to save the current state of the program to resume later.

## How-to:
You can use `Imbot` straight out of the box. The functionalities are pretty straightforward and documented.

## Installation and usage:
If you want to use `Imbot`, clone the repository, and then install the `maven` project locally by running:
```console
$ mvn clean install
```

And then add it as a dependency to your project / program:
```xml
<dependency>
    <groupId>telos-matter</groupId>
    <artifactId>imbot</artifactId>
    <version>0.1</version>
</dependency>
```
