package com.project.sushi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.sushi.R;

public class CuttingBoard extends View implements OnTouchListener{

	protected int drawColor = Color.WHITE; 
	protected int size = 7; 
	
	public List<Point> pdrawn = new ArrayList<Point>();
	public Stack<Point> asdf = new Stack<Point>(); 
	public Path path;
	public Drawable circle;
	public Drawable[] sushi_images;
	public TextView scoreboard;
	public ImageView feedback;
	public TextView remaining;
	public MediaPlayer playSound;
	public MediaPlayer mpFail = MediaPlayer.create(getContext(), R.raw.miss);
	
	//parameters to manipulate between levels
	private int currentLevel = 1;
	private int sushiSliced = 10;//for a game over / end screen.
	private int sushiDropped = 0;
	private int offset = 150;
	
	private int startY = 0;
	private int startX = 0;
	private int incX = 0; 
	private int incY = 0; 
	
	private int startTi = 5;
	private Random random = new Random();
	Collision col = new Collision(); 
	boolean checkCollide = false; 
	boolean addPoint = false; 
	boolean left = true;
	
	public double totalScore; 
	private HashMap<String, Integer> ingredients = new HashMap<String, Integer>(); //contains what you have cut and haven't changed into recipes
	private ArrayList<Cuttable> recipes ;  //recipes for sushi
	private Random ingRand = new Random(); //random generator to be used with toBeSpawn
	private Cuttable currentCuttable; //current object flying
	
	//this probably don't need to be up here?
	private ArrayList<Cuttable> toBeSpawn; //array containing missing ingredients for recipes

	public CuttingBoard(Context context) {
		super(context);
		init();
	}
	
	public CuttingBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CuttingBoard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	protected void init(){
		this.currentCuttable =  new Cuttable("ingredient1");
		toBeSpawn = new ArrayList<Cuttable>();
		recipes =  new ArrayList<Cuttable>(); 
		this.setOnTouchListener(this);
		Resources res = getResources();
		sushi_images = new Drawable[]
				{res.getDrawable(R.drawable.sushi),res.getDrawable(R.drawable.sushi1), res.getDrawable(R.drawable.sushi2)};

		circle = res.getDrawable(currentCuttable.getImage());
		if(currentCuttable.getSound() >= 0){
			playSound = MediaPlayer.create(getContext(), currentCuttable.getSound());
		}
	
		startY = random.nextInt(500)+(getWidth()/2); //getHeight()/2; 
		startX = getHeight();
		

		//add in all possible ingredients with 0
		ingredients.put("ingredient1", 0);
		ingredients.put("ingredient2", 0);
		ingredients.put("nori", 0);
		ingredients.put("sashimi", 0);
		ingredients.put("rawseaweed", 0);
		ingredients.put("livefish", 0);
		
		//initialize recipes and add in what they need
		HashMap<String, Integer> recipe1 = new HashMap<String, Integer>(); 
		recipe1.put("ingredient1",1);
		recipe1.put("ingredient2",1);
		
		HashMap<String, Integer> recipe2 = new HashMap<String, Integer>(); 
		recipe2.put("rawseaweed",1);
		recipe2.put("livefish",1);

		//add each recipe into recipes
		recipes.add(new Cuttable("sushi", R.drawable.sushi, recipe1));
		recipes.add(new Cuttable("logo", R.drawable.logo, recipe2));
	}
	
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas){
		//Check for collisions
		if(pdrawn.size() >=2 && addPoint){
			//if(!pdrawn.get(pdrawn.size()-1).getFirst() && !pdrawn.get(pdrawn.size()-2).getFirst()){
				checkCollide = col.checkCollisionsVectors(pdrawn.get(pdrawn.size()-2), pdrawn.get(pdrawn.size()-1), startX+incX+offset, startY+incY+offset, offset);
				addPoint = false; 
			//}
		}
		
		Paint p = new Paint();
		Resources res = getResources();
		// Reset the coordinates of the new object if it goes off the screen
		// Note: This is the same instance
		if (startY+incY < 0 || startX+incX > getWidth() || startY+incY > getHeight() || startX+incX < 0 || checkCollide) {
			Log.v("ingredient1", Integer.toString(ingredients.get("ingredient1")));
			Log.v("ingredient2", Integer.toString(ingredients.get("ingredient2")));
			boolean checkProc = currentCuttable.needsProcessing(); 
			if(checkCollide){
				checkCollide = false; 
				if(checkProc){
				//switch images
					boolean checkSwitched = currentCuttable.processIngredient(); 
					if(checkSwitched){
						circle = res.getDrawable(currentCuttable.getImage());
						//increment ingredients b/c it got hit and processed
						ingredients.put(currentCuttable.getPrevName(), ingredients.get(currentCuttable.getPrevName()) + 1); 
						
						double currentScore = col.getScore();
						totalScore += currentScore; // user's total score
							
						try {
							List<Integer> scoreList = LeaderBoard.getScoresList();
							Integer thresholdScore = scoreList.get(4);
							if (((int) totalScore) > thresholdScore){
								scoreList.set(4, ((int) totalScore));
								LeaderBoard.setScoresList(scoreList);
								LeaderBoard.saveList(scoreList, LeaderBoard.arrayName, this.getContext());
							}
						}
						catch (Exception e) {
							Log.v(e.toString(), e.toString());
						}
						sushiSliced--;
						updateScore();
						
						if(sushiSliced == 0){

							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

							// set dialog message
							alertDialogBuilder
									.setCancelable(false)
									.setPositiveButton("Next Round!",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int id) {
													nextLevel();
												}
											});
							
							AlertDialog alertDialog = alertDialogBuilder.create();
							
							alertDialog.show();
					
						}
							
						//visual feedback
						if(currentScore > 0 && currentScore < 900){
							feedback.setImageResource(R.drawable.nice);
						}
						else if(currentScore >= 900 && currentScore < 930){
							feedback.setImageResource(R.drawable.goodjob);
						}
						else if(currentScore >= 930 && currentScore < 960){
							feedback.setImageResource(R.drawable.excellent);
						}
						else if(currentScore >= 960 && currentScore < 990){
							feedback.setImageResource(R.drawable.amazing);
						}
						else if(currentScore >= 990 && currentScore <= 1000){
							feedback.setImageResource(R.drawable.perfect);
						}
						playSound.start();

						invalidate();
					}
				}
				
			}
		
			else if(startY+incY < 0 || startX+incX > getWidth() || startY+incY > getHeight() || startX+incX < 0 ){
				//off screen
				regenerateSushi(); 
			}
		}
		

		for(int i = 0; i < pdrawn.size(); i++){
			Point pt = pdrawn.get(i);
			p.setColor(pt.getColor());
			//canvas.drawCircle(pt.getX(),pt.getY(),pt.getSize(),p);
			if(!pt.getFirst() && i != 0){
				canvas.drawLine( (float)pdrawn.get(i-1).getX(), (float)pdrawn.get(i-1).getY(), (float)pt.getX(), (float)pt.getY(),p);
			}
			else{
				p.setStrokeWidth(pt.getSize());
			}
			
		}
		
		
		circle.setBounds(startX+incX, startY+incY, startX+offset+incX+50, startY+offset+incY+50);
		
		circle.draw(canvas);
		
	}	
	
	
	private void regenerateSushi(){
		//regenerate sushi b/c off screen
		generateStartingPosition(); 
		Resources res = getResources();

		MainActivity.ti = startTi; // reset time to zero
		incX = 0; // reset everything
		incY = 0; // reset everything
		
		if(currentLevel > 1 && sushiDropped == 0){
			Intent gameOver = new Intent (getContext(), GameOver.class);
			getContext().startActivity(gameOver);
		}
		
		if(currentCuttable.needsProcessing()){
			sushiDropped--;
			feedback.setImageResource(R.drawable.tryagain);
			mpFail.start();
		}
		checkCollide = false; 
		
		//check if there's a recipe that has been fulfilled
		boolean finished = false; //boolean for if there's a recipe finished
		for (int i = 0; i < recipes.size(); i++){
			if(recipes.get(i).checkRecipeMade(ingredients)){
				circle = res.getDrawable(recipes.get(i).getImage()); //make the next the recipe final product
				//decrement ingredients by recipe specifications
				Iterator<Entry<String, Integer>> it = (recipes.get(i)).getRecipe().entrySet().iterator(); 
				while(it.hasNext()){
					Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next(); 
					ingredients.put(pairs.getKey(), ingredients.get(pairs.getKey()) - pairs.getValue() );
				}
				finished = true; 
				Log.v(recipes.get(i).getName(), "recipeName");
				break; 
			}
		}
		Log.v("ingredient1", Integer.toString(ingredients.get("ingredient1")));
		Log.v("ingredient2", Integer.toString(ingredients.get("ingredient2")));
		Log.v("fish", Integer.toString(ingredients.get("livefish")));
		Log.v("seaweed", Integer.toString(ingredients.get("rawseaweed")));
		
		if(finished == false){
			//need to spawn random ingredient to get any recipe made
			for(int i = 0; i < recipes.size(); i++){
				toBeSpawn.addAll(recipes.get(i).getMissingIng(ingredients));
			}
			int randomIngredient = ingRand.nextInt(toBeSpawn.size());
			currentCuttable = toBeSpawn.get((randomIngredient));
			circle = res.getDrawable(toBeSpawn.get((randomIngredient)).getImage());
			if(currentCuttable.getSound() >= 0){
				playSound = MediaPlayer.create(getContext(), currentCuttable.getSound());
			}
			toBeSpawn = new ArrayList<Cuttable>(); 
		}
		Log.v( currentCuttable.getName(), "currentCuttable After");

		invalidate(); 
	}
	
	private void generateStartingPosition(){
		int yGeneration = random.nextInt(3);
		MainActivity.Vy = -(getHeight()/25 + random.nextInt((getHeight()/50)+1)); // reset to default value
		MainActivity.Vx = -(getWidth()/100 + random.nextInt((getWidth()/100)+1));
		switch (yGeneration){
			case 0:
				startY = getHeight();
				if(random.nextBoolean()){
					left = true;
					//startX = random.nextInt(getWidth()/4);
					startX = random.nextInt(getWidth());
				}
				else{
					left = false;
					//startX = getWidth() - random.nextInt(getWidth()/4);
					startX = getWidth() - random.nextInt(getWidth());
				}
				
				break; 
			case 1:
				left = true;
				startY = random.nextInt(2*getHeight()/4) + getHeight()/4;
				startX = 0; 
				generateVyFromWalls();
				//MainActivity.Vx *= random.nextInt(3*getHeight()/4) / getHeight(); 
				break;
			case 2:
				left = false; 
				startY = random.nextInt(2*getHeight()/4) + getHeight()/4;
				startX = getWidth(); 
				generateVyFromWalls();
				//MainActivity.Vy = -1 * random.nextInt((int)Math.sqrt(2* (getHeight() - startY) - 1)); 
				//MainActivity.Vx *= random.nextInt(3*getHeight()/4) / getHeight(); 
				break;
		}
	}
	
	private void generateVyFromWalls(){
		MainActivity.Vy = -1 * random.nextInt((int)Math.sqrt(2* (getHeight() - startY) - 1 )); 
		double vsquared = MainActivity.Vy*MainActivity.Vy + MainActivity.Vx*MainActivity.Vx;
		double range = vsquared;
		double angle = 90; 
		if(MainActivity.Vx < 0){
			angle = 180 + Math.atan(MainActivity.Vy / MainActivity.Vx);
		}
		else if (MainActivity.Vx < 0){
			angle = Math.atan(MainActivity.Vy / MainActivity.Vx);
		}
		range = vsquared*Math.sin(2*angle);
		/*Log.v("range: ", Double.toString(range));
		Log.v("Start x: ", Double.toString(startX));
		Log.v("Start y: ", Double.toString(startY));
		Log.v("V x: ", Double.toString(MainActivity.Vx));
		Log.v("V y: ", Double.toString(MainActivity.Vy));
		Log.v("V 2: ", Double.toString(vsquared));
		Log.v("angle: ", Double.toString(angle));
		*/
		if (range < 100){
			
			//Log.v("max x: ", Double.toString(getWidth()));
			//Log.v("max y: ", Double.toString(getHeight()));
			generateVyFromWalls();
		}
	}
	
	public boolean onTouch(View view, MotionEvent event){
		if (event.getAction() == MotionEvent.ACTION_DOWN ) {
			pdrawn.clear();
			Point newP = new Point(event.getX(),event.getY(),drawColor,size, true); 
			pdrawn.add(newP);
			//Log.v(Float.toString(event.getX()), Float.toString(event.getY()));
			//Log.v("Action Down", "down");
			addPoint = true; 
			invalidate();
		}
		/*else if (event.getAction() == MotionEvent.ACTION_MOVE ) {
			//pdrawn.clear();
			Point newP = new Point(event.getX(),event.getY(),drawColor,size, true); 
			pdrawn.add(newP);
			Log.v(Float.toString(event.getX()), Float.toString(event.getY()));
			Log.v("action Move", "move");
			addPoint = true; 
			invalidate();
		}*/
		else if (event.getAction() != MotionEvent.ACTION_UP){
			Point newP = new Point(event.getX(),event.getY(),drawColor,size, false); 
			pdrawn.add(newP);
			//Log.v("tag", circle.getBounds().toString());
			//Log.v("action up", "up");
			addPoint = true; 
			invalidate();
		}
		
		
		return true; 
		
	}
	
	public void clearPoints(){
		pdrawn = new ArrayList<Point>(); 
	}
	
	public void setColor(int a){
		drawColor = a; 
	}
	
	public void setSize(int a){
		size = a; 
	}
	
	public void increaseX(int value){
		incX += value;
	}
	
	public void decreaseX(int value){
		incX -= value;
	}
	
	public void increaseY(int value){
		incY += value;
	}
	
	public boolean isLeft(){
		return left;
	}
	
	public void biggerSushi(){
		offset += 50;
	}
	public void smallerSushi(){
		if(offset > 50){
			offset -= 50;
		}
	}
	
	public void setText(){
		scoreboard.setText(Double.toString(totalScore));
		if(currentLevel > 1){
		remaining.setText(Integer.toString(sushiSliced)+  " Dropped:" + Integer.toString(sushiDropped));
		}
		else{
		remaining.setText(Integer.toString(sushiSliced));
		}
		
	}
	public void updateScore(){
		if(scoreboard != null){
			scoreboard.setText(Double.toString(totalScore)); //+ "\t Remaining Sushi: " + sushiSliced);
			remaining.setText(Integer.toString(sushiSliced));
		}
	}
	
	public boolean isWin(){
		return sushiSliced == 0;	
	}
	
	public boolean isLoss(){
		return (sushiDropped == 0 && currentLevel > 1);
	}
	
	public void nextLevel(){
		if(currentLevel == 1){
			sushiSliced = 10;
			sushiDropped = 10;
		} else{
			sushiSliced = 10 + 2*(currentLevel-2);
			sushiDropped = 10 - (currentLevel-1);
			offset -= 10;
			MainActivity.Vy -= 10;
		}
		currentLevel++;
	}
	
	//TO BE FURTHER TESTED
	public void initialize(){
		MainActivity.Vy = -(getHeight()/25 + random.nextInt((getHeight()/50)+1)); // reset to default value
		MainActivity.Vx = -(getWidth()/100 + random.nextInt((getWidth()/100)+1));
		startX = random.nextInt(500)+(getWidth()/2);
		startY = getHeight();
		incX = 0;
		incY = 0;
	}

}
