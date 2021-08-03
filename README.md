# 159.261 Game Programming (Assignment 1)
**Copyright (c) 2021, Damian Coventry**

**All rights reserved**

## Brief
This is an implementation of a classic "snake game". The idea of a snake game originated in 1976 within an arcade game named [Blockade](https://en.wikipedia.org/wiki/Blockade_(video_game)).

The snake game type has subsequently spawned many, many clones, and even spawned its own [genre](https://en.wikipedia.org/wiki/Snake_(video_game_genre)). 

This implementation is more closely modelled upon [nibbles.bas](https://en.wikipedia.org/wiki/Nibbles_(video_game)) from Rick Raddatz in 1991 than the classic game Blockade.

![Main Menu](/ScreenShot0.png "Main Menu")
![Loading Screen](/ScreenShot1.png "Loading Screen")
![In Game](/ScreenShot2.png "In Game")

## Massey University Disclaimer
This is an assignment for Massey University course 159.261. Under no circumstances is it to be copied and submitted anywhere as plagiarised work. It is work created solely by me as original work. It has been submitted for 159.261 Game Programming S2 2021.  

## Game Features
 - Simple menu system
 - Single player and two players support
 - Application state machine: get ready, playing game, level complete, etc
 - Apples that must be be collected in sequence 1-9
 - 10 levels
 - Walls randomly inserted during the map
 - Power ups (inc speed, dec speed, inc points, dec points, inc snakes, dec snakes, dec length, random)

## Engine Features
 - LWJGL integration
 - OpenGL 4.5, with GLSL shaders
 - Directional specular lighting
 - Mix of 2D and 3D graphics
 - Bullet3 integration (used for Snake deaths)

## Building and running the project
I built this will IntelliJ, but it also builds with Eclipse.

### Dependencies
 - [Libbulletjme](https://github.com/stephengold/Libbulletjme). This project provides the ability to use the Bullet Physics engine within Java.
 - [LWJGL - Lightweight Java Game Library 3](https://github.com/LWJGL/lwjgl3/releases). This project provides access to OpenGL and GLSL shaders.
 - [JOML – Java OpenGL Math Library](https://github.com/JOML-CI/JOML). This project provides Vector and Matrix maths routines that slot in well to LWJGL.

### Setup
I created a local directory named `C:\Development\159.261-Game-Prog-Assgn1`. I created a subdirectory within this directory named `lib`.

The JDK I'm using is `openjdk-16`.

### Libbulletjme
Go to this link https://github.com/stephengold/Libbulletjme/releases/tag/10.5.0  
Download these files. Store them in the directory `lib`.  
`Libbulletjme-10.5.0.jar`  
`Libbulletjme-10.5.0-sources.jar`  
`Windows32DebugDp_bulletjme.dll`  
`Windows32DebugSp_bulletjme.dll`  
`Windows32ReleaseDp_bulletjme.dll`  
`Windows32ReleaseSp_bulletjme.dll`  
`Windows64DebugDp_bulletjme.dll`  
`Windows64DebugSp_bulletjme.dll`  
`Windows64ReleaseDp_bulletjme.dll`  
`Windows64ReleaseSp_bulletjme.dll`  

### LWJGL
Go to this link https://github.com/LWJGL/lwjgl3/releases  
Download the file `lwjgl-3.2.3.zip`.  
Unzip it into the directory `lib`. It will make 38 subdirectories.  
 
### JOML
Go to this link https://github.com/JOML-CI/JOML/releases/tag/1.10.1  
Download these files. Store them in the directory `lib`.  
`joml-1.10.1-sources.jar`  
`joml-1.10.1.jar`  
`joml-jdk8-1.10.1.jar`  

### Setup dependencies
#### IntelliJ
Click `File` -> `Project Structure` to open a dialog. From within the `Libraries` item, add the directory `lib` as a dependency, and also each of the `lwjglXXX` subdirectories within `lib` as dependencies.

Add a `Run/Debug Configuration` of type `Application`. Specify `com.snakegame.application.Application` as the entry point.

To build, click `Build` -> `Build Project`.

To run, click `Run` -> `Run XYZ`. Or just click the green run icon on the top right toolbar.

#### Eclipse
Click `File` -> `New` -> `New Project`. A dialog opens. I called the project `Snake`, and used the location `C:\Development\159.261-Game-Prog-Assgn1`. Click `Finish`.

In the top right of Eclipse, click `Open Perspective`. Double click `Java (default)`. The dialog closes.

Click `Run` -> `Run Configurations`. Select `Java Application` from the tree, then add a new configuration. I called mine simply `Java Application`, named the project `Snake`, and set the Main class to `com.snakegame.application.Application`.

Before closing this dialog, click the `Dependencies` tab. Select `Classpath Entries`, then click `Add JARs...`. I chose all JARs from within the `lib` directory (which is the JOML and Libbulletjme JARs.) Next I added all JARs from the following directories:  
`lib\lwjgl`  
`lib\lwjgl-glfw`  
`lib\lwjgl-opengl`  

To run the game click the green and white arrow on the main toolbar with the tooltip `Run Java Application`.

## Third Party Sources
### Programming
https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/  
https://github.com/lwjglgamedev/lwjglbook  

### Textures
https://icon-icons.com/icon/snake/100855  
https://icon-library.com/  
https://www.shutterstock.com/search/blue+snake+skin  
https://www.istockphoto.com/photos/tree-bark  
https://www.sketchuptextureclub.com/textures/architecture/concrete/plates/clean/clean-cinder-block-texture-seamless-01648  
https://www.pinterest.nz/pin/199213983490328795/  
https://www.shutterstock.com/search/red+timber  
https://www.istockphoto.com/illustrations/cardboard-box-texture  
https://www.istockphoto.com/photos/corrugated-iron  
https://www.textures.com/browse/dirt-roads/12455  
https://www.behance.net/gallery/14262663/Free-Bitmap-Grime-Textures  
https://www.pinterest.nz/pin/290341507232581683/  
https://www.123rf.com/photo_122593653_seamless-palm-tree-bark-background-tileable-trunk-texture-of-the-old-palm-tree-.html  
https://www.pngitem.com/so/tire-track/  
https://libreshot.com/green-leaves-texture/  
https://stock.adobe.com/nz/  
https://www.vectorstock.com/  
https://www.123rf.com/  
https://www.shutterstock.com/  
