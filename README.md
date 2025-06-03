# Term Project: Yunnori
### Yunnori game with Java SwingUI and FXUI

<details>
<summary>Folder Structure</summary>

  ```
  yunnori-game/
  ├── pom.xml
  └── src/
      └── main/
          └── java/
              └── yunnori/  <--------------------------------- Base package 'yunnori'
                  ├── Launcher.java
                  │
                  ├── core/  <---------------------------- Sub-package 'yunnori.core'
                  │   ├── GameLogicController.java
                  │   ├── Board.java
                  │   ├── Piece.java
                  │   ├── Team.java
                  │   ├── BoardType.java
                  │   ├── YunnoriRoll.java
                  │   └── YunnoriRoller.java
                  │
                  ├── swingui/  <------------------------- Sub-package 'yunnori.swingui'
                  │   ├── YunnoriSwingView.java
                  │   └── BoardPanel.java
                  │
                  └── fxui/     <------------------------- Sub-package 'yunnori.fxui'
                      ├── YunnoriFXView.java
                      └── BoardCanvas.java
  ```

</details>


### Here's the map in each version looks like:

- Rectangle Map
![Image](https://github.com/user-attachments/assets/3c1def41-1360-4907-82ab-a5e90f436f0e)


- Pentagon Map
![Image](https://github.com/user-attachments/assets/58962058-0940-441a-9ab6-6a6abc8553c0)


- Hexagon Map
![Image](https://github.com/user-attachments/assets/bf167b29-5f27-416d-a76d-1873c6b58569)
