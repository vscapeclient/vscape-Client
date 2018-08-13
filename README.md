# vscape custom client

Hey, here you will find the source code of my custom vscape client with some QoL features that you might find useful. with each new feature I will upload a binary file you can use, however feel free to compile from source and it will be exactly the same.

# Compiling
In order to compile from source you will need a Java IDE, I am using IntelliJ IDEA, however you can use anything.

Steps to compile are:

 - Download the source code from this repo
 - Import the source code into your IDE of choice
 - Resolve the dependancy issue with gson-2.8.0, download the jar from an official source and update the path.
 - Create a build that use com.runescape.Client as the main class, and you should be able to compile.
 - Find the below youtube videos for either Eclipse or IntelliJ IDEA on how to compile and run:
 - [Eclipse](https://streamable.com/8fanv)
 - [IntelliJ IDEA](https://streamable.com/oeqku)
# Getting NPC HP View to work
Due to the way vscape sends packets, they do not send the NPC's max health/current health in a packet, instead just percentages. Because of this, I have included a seperate JSON file that is loaded that includes all of the NPC health information. Please ensure that this file is located in the following folder structure relative to the .jar:

    /data/npcs/npcCombatDefs.json
    /data/npcs/npcDefinitions.json

This should allow the HP view to work fully.
# Features

The following features are currently implemented:

 - Space bar to continue dialogue
 - Dropped items displayed with name and amount (can turn off/on in settings.ini)
 - Bank searching [example here](https://streamable.com/j8nuw)
 - Real time NPC HP View

# FAQ

 - Why does this client compile to be larger than the official vscape client? - The reason the client is larger than the vscape client is due to an additionally loaded library called gson-2.8.0.jar, this is an official library from Google themselves that allows parsing and working with JSON files. The reason this library is needed is for the loading of NPC Definitions for the HP View.
 - How do I know this client is safe? You do not need to download the pre-compiled binary to run this, you are welcome to follow the instructions above to compile the client yourself and run it after looking through the code. Here is a video of the precompiled binary running and all network traffic alongside it. [video here](https://streamable.com/fqrgb)
