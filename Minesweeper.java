import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

class Game extends World {
  MineSweepeModel model;
  int x;
  int y;
  int numOfMines;
  int cellSide;
  int flagNumber;
  WorldScene scene;
  int tick;
  boolean start;
  boolean win;

  Game(int x, int y, int numOfMines, int cellSide) {
    if (x < 14) {
      throw new IllegalArgumentException("width should not smaller than 14");
    }
    if (numOfMines > x * y) {
      throw new IllegalArgumentException("Mines should not larger than number of cells");
    }
    if (cellSide < 15) {
      throw new IllegalArgumentException("Side is too small to see");
    }
    this.x = x;
    this.y = y;
    this.numOfMines = numOfMines;
    this.cellSide = cellSide;
    this.flagNumber = numOfMines;
    this.tick = 0;
    this.start = false;
    this.win = false;
    this.model = new MineSweepeModel(this.x, this.y, 
        this.numOfMines, this.cellSide, this.flagNumber);
  }

  // count the number of flags;
  String countFlag(int flagNumber) {
    if (flagNumber >= 100 && flagNumber <= 999) {
      return Integer.toString(flagNumber);
    }
    if (flagNumber < 100 && flagNumber >= 10) {
      return 0 + Integer.toString(flagNumber);
    }
    if (flagNumber < 10 && flagNumber >= 0) {
      return "00" + Integer.toString(flagNumber);
    }
    if (flagNumber > 999) {
      throw new IllegalArgumentException("no more than 999");
    }
    if (flagNumber < 0 && flagNumber > -10) {
      return "- 0" + Integer.toString(-flagNumber);
    }
    if (flagNumber <= -10 && flagNumber >= -99) {
      return "- " + Integer.toString(-flagNumber);
    }
    if (flagNumber < -99) {
      return "- 99";
    }
    else {
      return "000";
    }
  }

  // count the time
  public void onTick() {
    if (this.start && !this.model.gameEnd) {
      this.tick = this.tick + 1; 
    }
  }

  // display the ticks
  WorldImage displayTicks() {
    if (this.tick < 10) {
      return new TextImage("00" + this.tick, 30, FontStyle.BOLD, Color.red);
    }
    if (this.tick >= 10 
        && this.tick < 100) {
      return new TextImage("0" + this.tick, 30, FontStyle.BOLD, Color.red);
    }
    if (this.tick >= 100 
        && this.tick < 999) {
      return new TextImage(Integer.toString(this.tick), 30, FontStyle.BOLD, Color.red);
    }
    if (this.tick >= 999) {
      return new TextImage("999", 30, FontStyle.BOLD, Color.red);
    }
    else {
      return null;
    }
  }

  // renders the world
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();
    WorldImage remindFlags = new TextImage(this.countFlag(this.model.flagNumber), 
        30, FontStyle.BOLD, Color.RED);
    WorldImage restartOutline = new RectangleImage(40, 40, OutlineMode.OUTLINE, Color.darkGray);
    WorldImage restartBlank = new OverlayImage(restartOutline,
        new RectangleImage(40, 40, OutlineMode.SOLID, Color.lightGray));
    WorldImage countOutline = new RectangleImage(60, 40, OutlineMode.OUTLINE, Color.darkGray);
    WorldImage countBlank = new OverlayImage(countOutline,
        new RectangleImage(60, 40, OutlineMode.SOLID, Color.darkGray));
    WorldImage flagCount = new OverlayImage(remindFlags, countBlank);
    WorldImage restartPart = new OverlayImage(new CircleImage(15, OutlineMode.SOLID, Color.yellow),
        restartBlank);
    WorldImage tickCount = new OverlayImage(this.displayTicks(), countBlank);

    scene.placeImageXY(
        this.model.drawList(),
        this.x * this.cellSide / 2,
        this.y * this.cellSide / 2 + 50);
    scene.placeImageXY(restartPart, this.x * this.cellSide / 2, 25);
    scene.placeImageXY(flagCount, 35, 25);
    scene.placeImageXY(tickCount, this.x * this.cellSide - 65, 25);
    this.scene = scene;
    if (this.model.gameEnd && !this.win) {
      WorldImage loss = new TextImage("You Died!!!", 30, FontStyle.ITALIC, Color.RED);
      this.model.displayAllCells();
      this.scene.placeImageXY(loss, this.x * this.cellSide / 2, 25);
    }
    if (this.win && this.model.gameEnd) {
      WorldImage loss = new TextImage("You Are Alive!!!", 30, FontStyle.ITALIC, Color.RED);
      this.model.displayAllCells();
      this.scene.placeImageXY(loss, this.x * this.cellSide / 2,
          this.y * this.cellSide / 2  + 25);
    }
    return this.scene;
  }

  // mouse click on the board
  public void onMouseReleased(Posn pos, String buttonName) {
    double midWeight = this.model.width * this.model.cellSide / 2;
    double restartPosnRight = midWeight + 20;
    double restartPosnLeft = midWeight - 20;
    int restartPosnTop = 5;
    int restartPosnButton = 50 - 5;
    double px = pos.x;
    double py = pos.y;

    if (buttonName.equals("LeftButton")) {
      this.start = true;
      if (restartPosnLeft <= px
          && px <= restartPosnRight
          && restartPosnTop <= py
          && py <= restartPosnButton) {
        this.model = new MineSweepeModel(this.x, this.y, 
            this.numOfMines, this.cellSide, this.flagNumber);
        this.flagNumber = this.numOfMines;
        this.tick = 0;
        this.start = false;
        this.win = false;
      }
      if (!this.model.gameEnd) {
        if (py >= 50) {
          if (this.model.openTheCell(pos)) {
            this.model.gameEnd = true;
          }
          if (this.model.isGameEnding()) {
            this.model.gameEnd = true;
            this.win = true;
          }
        }
      }
    }
    if (buttonName.equals("RightButton") 
        && !this.model.gameEnd) {
      if (py >= 50) {
        this.model.flagOnCell(pos);
        if (this.model.isGameEnding()) {
          this.model.gameEnd = true;
          this.win = true;
        }
      }
    }
  }

  // change the end of world scene
  public void lastScenes(String msg) {
    WorldImage loss = new TextImage(msg, 30, FontStyle.BOLD, Color.RED);
    this.model.displayAllCells();
    this.scene.placeImageXY(loss, this.model.width * this.model.cellSide / 2, 25);
  }

}

class Cell {
  boolean hasMine;
  int num;
  ArrayList<Cell> neighbour;
  boolean opened;
  boolean hasFlag;
  boolean boomed;

  // Constructor only for test
  Cell(boolean hasMine) {
    this.hasMine = hasMine;
    this.num = 0;
    this.neighbour = new ArrayList<Cell>();
    this.opened = true;
    this.hasFlag = false;
    this.boomed = true;
  }

  // Constructor of Cell
  Cell() {
    this.hasMine = false;
    this.num = 0;
    this.neighbour = new ArrayList<Cell>();
    this.opened = false;
    this.hasFlag = false;
    this.boomed = false;
  }

  // counts the mines
  void countMines() {
    int total = 0;
    for (int i = 0; i < this.neighbour.size(); i++) {
      if (this.neighbour.get(i).hasMine) {
        total = total + 1;
      }
    }
    this.num = total;
  }

  // draws the cells
  public WorldImage drawCell(int cellSide, boolean gameEnd) {
    WorldImage emptyCellBack = new RectangleImage(
        cellSide, cellSide, OutlineMode.OUTLINE, Color.white);
    WorldImage emptyCell = new OverlayImage(new RectangleImage(
        cellSide, cellSide, OutlineMode.SOLID, Color.GRAY),emptyCellBack);
    WorldImage unOpenedCell = new OverlayImage(
        new RectangleImage(cellSide, cellSide, OutlineMode.OUTLINE, Color.white),
        new RectangleImage(cellSide, cellSide, OutlineMode.SOLID, Color.lightGray));
    WorldImage flag = new OverlayImage(new RegularPolyImage(
        cellSide / 4, 3, OutlineMode.SOLID, Color.red), unOpenedCell);
    WorldImage cross = new OverlayImage(new LineImage(new Posn(cellSide, cellSide), Color.black), 
        new LineImage(new Posn(cellSide, - cellSide), Color.black));
    if (this.opened) {
      if (this.hasMine && this.boomed) {
        return new OverlayImage(new CircleImage(cellSide * 1 / 4, OutlineMode.SOLID, Color.red),
            emptyCell);
      }

      if (this.hasMine) {
        return new OverlayImage(new CircleImage(cellSide * 1 / 4, OutlineMode.SOLID, Color.black),
            emptyCell);
      }

      if (this.num == 0) {
        return emptyCell;
      }
      else {
        return new OverlayImage(new TextImage(Integer.toString(this.num) , 
            cellSide * 4 / 5 , Color.BLUE),
            emptyCell);
      }
    }
    else if (this.hasFlag) {
      if  (gameEnd && !this.hasMine) {
        return new OverlayImage(cross,
            flag);
      }
      else {
        return flag;
      }
    }
    else {
      return unOpenedCell;
    }
  }

  // open the cell
  public void openIt() {
    if (this.num == 0 && !this.hasMine && !this.hasFlag) {
      this.opened = true;
      this.openItHelp(this.neighbour);
    }
    if (this.num != 0 && !this.hasFlag) {
      this.opened = true;
    }
    if (this.hasMine && !this.hasFlag) {
      this.opened = true;
      this.boomed = true;
    }
  }

  // open its neighbours
  public void openItHelp(ArrayList<Cell> list) {
    ArrayList<Cell> needOpen = new ArrayList<Cell>();
    for (int i = 0; i < list.size(); i++) {
      Cell neighbourCell = list.get(i);
      if (neighbourCell.num == 0
          && !neighbourCell.hasMine
          && !neighbourCell.hasFlag) {
        needOpen.add(list.remove(i));
        i = i - 1;
      }
      if (neighbourCell.num != 0 
          && !neighbourCell.hasFlag) {
        neighbourCell.opened = true;
      }
    }
    for (int p = 0; p < needOpen.size(); p++) {
      needOpen.get(p).openIt();
    }
  }

  // set a flag
  public int flagIt() {
    if (this.hasFlag && !this.opened) {
      this.hasFlag = false;
      return 1;
    }
    else if (!this.hasFlag && !this.opened) {
      this.hasFlag = true;
      return -1;
    }
    return 0;
  }
}

class MineSweepeModel {
  int width;
  int hight;
  ArrayList<ArrayList<Cell>> board;
  int numOfMines;
  int cellSide;
  int flagNumber;
  boolean gameEnd;

  // Constructor of MineSweepeModuel
  MineSweepeModel(int width, int hight, int nom, int cellSide, int flagNumber) {
    this.width = width;
    this.hight = hight;
    this.numOfMines = nom;
    this.cellSide = cellSide;
    this.flagNumber = flagNumber;
    this.gameEnd = false;
    this.board = new ArrayList<ArrayList<Cell>>();
    this.createEmptyBoard();
    this.spawnMines();
    this.link();
    this.setUpMinesCount();
  }

  // set a flag on the cell
  public void flagOnCell(Posn pos) {
    Cell chosenCell = this.board.get(
        pos.x / this.cellSide).get(
            (pos.y - 50) / this.cellSide);

    this.flagNumber = this.flagNumber + chosenCell.flagIt();
  }

  // open the Cell according to pos
  public boolean openTheCell(Posn pos) {
    Cell chosenCell = this.board.get(
        pos.x / this.cellSide).get(
            (pos.y - 50) / this.cellSide);

    chosenCell.openIt();
    return chosenCell.boomed;
  }

  // helps draw the board
  public WorldImage drawList() {
    WorldImage newBoard = new EmptyImage();
    for (int i = 0; i < width; i++) {
      WorldImage verticalLine = new EmptyImage();
      for (int p = 0; p < hight; p++) {  
        Cell c = this.board.get(i).get(p);
        verticalLine = new AboveAlignImage(
            AlignModeX.RIGHT, verticalLine, c.drawCell(this.cellSide, this.gameEnd));
      }
      newBoard = new BesideAlignImage(AlignModeY.TOP ,newBoard, verticalLine);
    }
    return newBoard;
  }

  // helps create the board
  void createEmptyBoard() {
    for (int i = 0; i < width; i = i + 1) {
      ArrayList<Cell> vertical = new ArrayList<Cell>();
      for (int p = 0; p < hight; p = p + 1) {
        vertical.add(new Cell());
      }
      this.board.add(vertical);
    }
  }

  // links the cells together
  void link() {
    for (int i = 0; i < width; i++) {
      for (int p = 0; p < hight; p++) {
        Cell c = this.board.get(i).get(p);
        int left = i - 1;
        int right = i + 1;
        int up = p - 1;
        int down = p + 1;

        if (left >= 0 && up >= 0) {
          c.neighbour.add(this.board.get(left).get(up));
        }
        if (up >= 0) {
          c.neighbour.add(this.board.get(i).get(up));
        }
        if (right < width && up >= 0) {
          c.neighbour.add(this.board.get(right).get(up));
        }
        if (right < width) {
          c.neighbour.add(this.board.get(right).get(p));
        }
        if (right < width && down < hight) {
          c.neighbour.add(this.board.get(right).get(down));
        }
        if (down < hight) {
          c.neighbour.add(this.board.get(i).get(down));
        }
        if (left >= 0 && down < hight) {
          c.neighbour.add(this.board.get(left).get(down));
        }
        if (left >= 0) {
          c.neighbour.add(this.board.get(left).get(p));
        }
      }
    }
  }

  // spawn the mines
  void spawnMines() {
    Random rand = new Random();

    for (int i = 0; i < this.numOfMines; i = i + 1) {
      int randX = rand.nextInt(this.width); 
      int randY = rand.nextInt(this.hight);
      Cell c = this.board.get(randX).get(randY);

      if (!c.hasMine) {
        c.hasMine = true;
      }
      else {
        i = i - 1;
      }
    }
  }

  // counts the number of mines neighboring a particular cell
  void setUpMinesCount() {
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.hight; p++) {
        this.board.get(i).get(p).countMines();
      }
    }
  }

  // display all the Mines in the end
  void displayAllCells() {
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.hight; p++) {  
        Cell c = this.board.get(i).get(p);
        if (c.hasMine && !c.hasFlag && !c.opened) {
          c.opened = true;
        }
      }
    }
  }

  // ask is the Game ending with winning
  boolean isGameEnding() {
    boolean state = false;
    int count = 0;
    int allOpend = 0;
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.hight; p++) {
        Cell c = this.board.get(i).get(p);
        if (!c.opened) {
          if (c.hasMine && c.hasFlag) {
            count = count + 1;
          }
        }
        if (c.opened && !c.hasMine) {
          allOpend = allOpend + 1;
        }
        if (count == this.numOfMines 
            && allOpend == this.width * this.hight - this.numOfMines) {
          state = true;
        }
      }
    }
    return state;
  }
}

class ExampleMineSweeper {
  int x = 30;
  int y = 16;
  int singleCellWidth = 20;
  int numOfMines = 99;

  Cell c = new Cell();

  ArrayList<ArrayList<Cell>> mt = new ArrayList<ArrayList<Cell>>();

  MineSweepeModel testEmpty = new MineSweepeModel(2, 2, 0, this.singleCellWidth, 0);

  ArrayList<Cell> horiCell = new ArrayList<Cell>();
  ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();

  // tests emptyBoard
  void testEmptyBoard(Tester t) {
    t.checkExpect(testEmpty.board.size(), 2 );

    testEmpty.createEmptyBoard();

    t.checkExpect(testEmpty.board.size(), 4);
    t.checkExpect(testEmpty.board.get(0).size(), 2);
  }

  // tests drawCell
  boolean testDrawCell(Tester t) {
    WorldImage emptyCellBack = new RectangleImage(
        this.singleCellWidth, this.singleCellWidth,
        OutlineMode.OUTLINE, Color.white);
    WorldImage rec = new OverlayImage(new RectangleImage(
        this.singleCellWidth, this.singleCellWidth,
        OutlineMode.SOLID, Color.GRAY),emptyCellBack);
    Cell c = new Cell();
    c.opened = true;
    return t.checkExpect(c.drawCell(this.singleCellWidth, false).equals(rec), true);
  }

  // tests displayCells
  boolean testDisplayAllCells(Tester t) {
    MineSweepeModel test = new MineSweepeModel(1, 1, 1, 20, 0);
    test.displayAllCells();
    return t.checkExpect(test.board.get(0).get(0).opened, true);
  }

  // tests flagOnCell
  boolean testFlagOnCell(Tester t) {
    MineSweepeModel test = new MineSweepeModel(1, 1, 0, 20, 0);
    test.flagOnCell(new Posn(0, 50));
    return t.checkExpect(test.board.get(0).get(0).hasFlag, true);
  }

  //tests openTheCell
  boolean testOpenTheCell(Tester t) {
    MineSweepeModel test = new MineSweepeModel(1, 1, 0, 20, 0);
    test.openTheCell(new Posn(0, 50));
    return t.checkExpect(test.board.get(0).get(0).opened, true);
  }
  
  // test Counter for mines
  void testCountMines(Tester t) {
    Cell c = new Cell();
    c.num = 3;
    t.checkExpect(c.num, 3);
    c.countMines();
    t.checkExpect(c.num, 0);
  }
  
  // flag the Cell;
  void testFlagIt(Tester t) {
    Cell c = new Cell();
    t.checkExpect(c.hasFlag, false);
    c.flagIt();
    t.checkExpect(c.hasFlag, true);
  }
  
  // open the Cell
  void testOpenIt(Tester t) {
    Cell c = new Cell();
    t.checkExpect(c.opened, false);
    c.openIt();
    t.checkExpect(c.opened, true);
  }

  // runs the game
  void testRunGame(Tester t) {

    World minesweeper = new Game(this.x, this.y, this.numOfMines, this.singleCellWidth);
    minesweeper.bigBang(this.x * this.singleCellWidth, this.y * this.singleCellWidth + 50, 1.0); 
  }

  public static void main(String args[]) {
    int x = 30;
    int y = 16;
    int singleCellWidth = 20;
    int numOfMines = 99;
    World minesweeper = new Game(x, y, numOfMines, singleCellWidth);
    minesweeper.bigBang(x * singleCellWidth, y * singleCellWidth + 50, 1.0);
  }
}
