# Obliteration_JAVA
A GUI game created with threads and sockets

## Instructions to start the game on the same computer
- Download the file 'Obliteration_local_mode.jar' and open it twice by double-click.

## Instructions to start the game on two different computers
- Both computers must be in the same newtork.
- Download the file ‘Obliteration_LAN_mode.jar’.
- In one of the computers, open the command prompt, type the command ‘ipconfig’ and copy the IP. 
- Open the command prompt on the second computer.
- On both computers, go to the directory of the game and type the command ‘java -jar Obliteration_LAN_mode.jar”.
- Type the IP copied from the first computer.
- Type a port number between 1 and 65535. Both computers must have the same number. The recommendation is 22222.
- If you get a message like: “Error: 192.168.1.1:22222 doesn’t exist or is already in use.” you may mistype the IP or port. They can be cases where there are already two players in the game, or the port is in use by the computer. In those case, try again with a different port number.
