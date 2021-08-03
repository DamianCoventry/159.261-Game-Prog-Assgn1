# 159.261 Game Programming (Assignment 1)
**Copyright (c) 2021, Damian Coventry**

**All rights reserved**

## Brief
This is an implementation of a classic "snake game". The idea of a snake game originated in 1976 within an arcade game named [Blockade](https://en.wikipedia.org/wiki/Blockade_(video_game)).

The snake game type has subsequently spawned many, many clones, and even spawned its own [genre](https://en.wikipedia.org/wiki/Snake_(video_game_genre)). 

This implementation is more closely modelled upon [nibbles.bas](https://en.wikipedia.org/wiki/Nibbles_(video_game)) from Rick Raddatz in 1991 than the classic game Blockade.

## Massey University Disclaimer
This is an assignment for Massey University course 159.261. Under no circumstances is it to be copied and submitted anywhere as plagiarised work. It is work created solely by me as original work. It has been submitted for 159.261 Game Programming S2 2021.  

## Game Features
 - Simple menuing system
 - Single player and two players support
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

### Libbulletjme
Go to this link https://github.com/stephengold/Libbulletjme/releases/tag/10.5.0
Download these files. Store them in the directory **lib**.
`Libbulletjme-10.5.0.jar`
`Libbulletjme-10.5.0-javadoc.jar`
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
Download the file `lwjgl-3.2.3.zip`
Unzip it into the directory **lib**.
 
### JOML
Go to this link https://github.com/JOML-CI/JOML/releases/tag/1.10.1
Download these files. Store them in the directory **lib**.
`joml-1.10.1-sources.jar`
`joml-1.10.1.jar`
`joml-jdk8-1.10.1.jar`

### Setup dependencies
Within IntelliJ, I used File -> Project Structure -> Libraries to add the directory `lib` as a dependency, and also, each of the lwjglXXX directories as dependencies.
