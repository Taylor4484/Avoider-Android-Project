     _             _     _           
    / \__   _____ (_) __| | ___ _ __ 
   / _ \ \ / / _ \| |/ _` |/ _ \ '__|
  / ___ \ V / (_) | | (_| |  __/ |   
 /_/   \_\_/ \___/|_|\__,_|\___|_|   
                                     
Avoider: An Android Accelerometer Game

==========================
         ABOUT
==========================

Avoider is an game created for the android operating system. 
The game is still in development, this is V1 which focused on
simply getting the game logic to work and interact with the phone's
accelerometer and audio manager.

This application was created by Kyrsten Kelly & Taylor McCaslin as a 
final project for CS 329E Elements of Mobile Computing at The University 
of Texas at Austin. 

They can be reached at: kyrsten.kelly@gmail.com, Taylor4484@gmail.com

==========================
    GAME DESCRIPTION	 
==========================

A poor lonely penguin has gotten lost on the ice, it's your job
to help it survive! By tilting the phone move the penguin around
the ice field to collect sunshine and avoid rain clouds.

As the game progresses (the longer you survive), the harder the game
becomes. Sun shines and clouds are added at a regular frequency. As more
clouds are added to the ice field, the harder it becomes to collect 
sun shines.

==========================
      GAME RULES
==========================

- For every 2 (two) sun shines collected, you will receive 1 extra life 
  (+ 1 red heart)
- For every 1 (one) rain cloud collided with, you will loose 1 life 
  (- 1 read heart)

- A new sunshine appears every 2 seconds
- A new rain cloud appears every 10 seconds

- You lose the game when you run out of lives
- You win the game when you collect 10 lives

==========================
GAME FEATURES
==========================

- Accelerometer control of game's main character
- Audio feedback on collision with sunshine or rain cloud

==========================
   FUTURE DEVELOPMENTS
==========================

We plan on expanding this game to having different levels. 
Each level will increase frequency clouds are added, and reduce
the frequency sun shines are added. Also the number of sun shines
required to gain an extra life will increase.

We also plan to add a high score (based on length of the game) 
using internal storage to record the player's name and score.


==========================
   GNU License
==========================

Avoider is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Avoider is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Avoider.  If not, see <http://www.gnu.org/licenses/>.
