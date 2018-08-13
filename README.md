# vscape custom client

Hey, here you will find the source code of my custom vscape client with some QoL features that you might find useful. with each new feature I will upload a binary file you can use, however feel free to compile from source and it will be exactly the same.

# Compiling
In order to compile from source you will need a Java IDE, I am using IntelliJ IDEA, however you can use anything.

Steps to compile are:

 - Download the source code from this repo
 - Import the source code into your IDE of choice
 - Resolve the dependancy issue with gson-2.8.0, download the jar from an official source and update the path.
 - Create a build that use com.runescape.Client as the main class, and you should be able to compile.
# Getting NPC HP View to work
Due to the way vscape sends packets, they do not send the NPC's max health/current health in a packet, instead just percentages. Because of this, I have included a seperate JSON file that is loaded that includes all of the NPC health information. Please ensure that this file is located in the following folder structure relative to the .jar:

    /data/npcs/npcCombatDefs.json
    /data/npcs/npcDefinitions.json

This should allow the HP view to work fully.
# Features

The following features are currently implemented:

 - Space bar to continue dialogue
 - Dropped items displayed with name and amount (can turn off/on in settings.ini)
 - Bank searching
 - Real time NPC HP View
