package com.example.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
//---Handles clicks on the game board buttons


public class MainActivity extends AppCompatActivity {

    // Represents the internal state of the game
    private TicTacToeGame mGame;
    // Game status text displayed
    private TextView mStatusTextView;
    // Buttons making up the board
    private Button mBoardButtons[];
    // Various text displayed
    private TextView mInfoTextView;
    // Restart Button
    private Button mStartButton;
    // Imageview for animation
    private ImageView mImageView;

    // Game Over
    Boolean mGameOver;

    // Game status: 0 -- win, 1 -- tie, 2 -- lose
    private int mGameStatusCount[];
    //who goes first
    private char mGoFirst;

    // Game difficulty level: Easy (Level 1), Harder (Level 2), or Expert(Level 3)
    private int mDifficultyLevel;


    // soundpool
    private SoundPool mSoundPool;
    private HashMap<Integer, Integer> mSoundMap;

    // animation when you win
    private AnimationDrawable mAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardButtons = new Button[mGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.button0);
        mBoardButtons[1] = (Button) findViewById(R.id.button1);
        mBoardButtons[2] = (Button) findViewById(R.id.button2);
        mBoardButtons[3] = (Button) findViewById(R.id.button3);
        mBoardButtons[4] = (Button) findViewById(R.id.button4);
        mBoardButtons[5] = (Button) findViewById(R.id.button5);
        mBoardButtons[6] = (Button) findViewById(R.id.button6);
        mBoardButtons[7] = (Button) findViewById(R.id.button7);
        mBoardButtons[8] = (Button) findViewById(R.id.button8);

        mStatusTextView = (TextView) findViewById(R.id.text_status);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mStartButton = (Button) findViewById(R.id.button_restart);
        mImageView = (ImageView) findViewById(R.id.ani_view);
        mImageView.setBackgroundResource(R.drawable.view_animation);
        mAnim = (AnimationDrawable) mImageView.getBackground();

        mGameStatusCount = new int[3];
        mGameStatusCount[0] = 0;
        mGameStatusCount[1] = 0;
        mGameStatusCount[2] = 0;

        mGoFirst = TicTacToeGame.HUMAN_PLAYER;
        mDifficultyLevel = 1; //Easy mode: Level 1
        mGame = new TicTacToeGame();
        initSoundPool();
        startNewGame();
    }
    public void initSoundPool()
    {
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundMap = new HashMap<Integer, Integer>();
        mSoundMap.put(1, mSoundPool.load(this, R.raw.move_sound, 1));
        mSoundMap.put(2, mSoundPool.load(this, R.raw.win_sound, 1));
        mSoundMap.put(3, mSoundPool.load(this, R.raw.tie_sound, 1));
        mSoundMap.put(4, mSoundPool.load(this, R.raw.lose_sound, 1));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.difficulty_settings:
                difficultySettingDialog();
                return true;
            case R.id.menu_about:
                openAboutDialog();
                return true;

            case R.id.menu_quit:
                finish();
                return true;
        }
        return false;
    }

    private void difficultySettingDialog() {
        final String[] difficultyLevels =  getResources().getStringArray(R.array.difficulty_levels);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.difficulty);
        alertBuilder.setSingleChoiceItems(difficultyLevels, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Toast.makeText(MainActivity.this, difficultyLevels[i], Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDifficultyLevel = i+1;
                startNewGame();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void openAboutDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.menu_about)
                .setMessage(R.string.about_msg)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {

                            }
                        }).show();
    }

    //--- Set up the game board.
    private void startNewGame() {
        mGameOver = false;
        mGame.clearBoard();
        //---Reset all buttons
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        mImageView.setVisibility(View.INVISIBLE);
        updateGameStatus();

        //---alternate who gets to go first
        mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
        if(mGoFirst == TicTacToeGame.HUMAN_PLAYER)
            mInfoTextView.setText(R.string.you_go_first);
        else{
            mInfoTextView.setText(R.string.machine_go_first);

            //computer make a move
            int move = mGame.getComputerMove(mDifficultyLevel);
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
            mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
            mInfoTextView.setText(R.string.your_turn);
        }
    }

    private class ButtonClickListener implements View.OnClickListener{
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        @Override
        public void onClick(View v) {
            if (mGameOver == false) {
                if (mBoardButtons[location].isEnabled()) {
                    mSoundPool.play(mSoundMap.get(1), 1, 1, 0, 0, 1);
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);
                    //--- If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
                        mInfoTextView.setText(R.string.machine_turn);

                        //computer make a move
                        int move = mGame.getComputerMove(mDifficultyLevel);
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                        winner = mGame.checkForWinner();
                    }


                    if (winner == 0) {
                        mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
                        mInfoTextView.setText(R.string.your_turn);
                    } else if (winner == 1) {
                        mSoundPool.play(mSoundMap.get(3), 1, 1, 0, 0, 1);

                        mInfoTextView.setTextColor(Color.rgb(0, 0, 200));
                        mInfoTextView.setText(R.string.tie);

                        mGameStatusCount[1] += 1;
                        mGameOver = true;
                        updateGameStatus();
                    } else if (winner == 2) {
                        mSoundPool.play(mSoundMap.get(2), 1, 1, 0, 0, 1);
                        mImageView.setVisibility(View.VISIBLE);
                        mAnim.start();
                        mInfoTextView.setTextColor(Color.rgb(0, 200, 0));
                        mInfoTextView.setText(R.string.you_win);

                        mGameStatusCount[0] += 1;
                        mGameOver = true;
                        updateGameStatus();
                    } else {
                        mSoundPool.play(mSoundMap.get(4), 1, 1, 0, 0, 1);

                        mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
                        mInfoTextView.setText(R.string.machine_win);

                        mGameStatusCount[2] += 1;
                        mGameOver = true;
                        updateGameStatus();
                    }
                }
            }
        }
    }
    private void updateGameStatus() {
        StringBuilder builder = new StringBuilder();
        final String[] gameStatusSet =  getResources().getStringArray(R.array.game_status_set);
        String statusTitle = getResources().getString(R.string.game_status);
        builder.append("<font color=\"#000000\">" + statusTitle + "</font><br>");
        builder.append("<font color=\"#00ff00\">" + gameStatusSet[0] + mGameStatusCount[0] + "</font>&emsp;");
        builder.append("<font color=\"#0000ff\">" + gameStatusSet[1] + mGameStatusCount[1] + "</font>&emsp;");
        builder.append("<font color=\"#ff0000\">" + gameStatusSet[2] + mGameStatusCount[2] + "</font>");
        mStatusTextView.setText(Html.fromHtml(builder.toString()));
    }
    // set UI game board move
    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        else
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
    }

    //--- OnClickListener for Restart a New Game Button
    public void newGame(View v) {
        if(mGoFirst == TicTacToeGame.HUMAN_PLAYER)
            mGoFirst = TicTacToeGame.COMPUTER_PLAYER;
        else
            mGoFirst = TicTacToeGame.HUMAN_PLAYER;
        startNewGame();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(mAnim.isRunning()){
            mAnim.stop();
        }
    }


    @Override
    protected void onDestroy() {
        mSoundPool.release();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("game_over", mGameOver);
        outState.putIntArray("status_count", mGameStatusCount);
        outState.putChar("go_first", mGoFirst);
        outState.putInt("difficulty_level", mDifficultyLevel);

        int human_move[] = new int[mGame.BOARD_SIZE];
        int computer_move[] = new int[9];
        for(int i = 0; i < mGame.BOARD_SIZE; ++i){
            String buttonText = mBoardButtons[i].getText().toString();
            if(buttonText.equals(String.valueOf(TicTacToeGame.HUMAN_PLAYER)))
                human_move[i] = 1;
            else
                human_move[i] = -1;

            if(buttonText.equals(String.valueOf(TicTacToeGame.COMPUTER_PLAYER)))
                computer_move[i] = 1;
            else
                computer_move[i] = -1;
        }


        outState.putIntArray("human_move", human_move);
        outState.putIntArray("computer_move", computer_move);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGameOver = savedInstanceState.getBoolean("game_over");
        mGameStatusCount = savedInstanceState.getIntArray("status_count");
        updateGameStatus();
        //mInfoTextView.setText(String.valueOf(mGameStatusCount[0]));
        mGoFirst = savedInstanceState.getChar("go_first");
        mDifficultyLevel = savedInstanceState.getInt("difficulty_level");

        int human_move[] = savedInstanceState.getIntArray("human_move");
        //mInfoTextView.setText(String.valueOf(human_move[0]));
        int computer_move[] = savedInstanceState.getIntArray("computer_move");
        int moveCount = 0;
        for(int i = 0; i < mGame.BOARD_SIZE; ++i) {
            if(human_move[i] == 1){
                setMove(TicTacToeGame.HUMAN_PLAYER, i);
                moveCount += 1;
            }
            if(computer_move[i] == 1)
                setMove(TicTacToeGame.COMPUTER_PLAYER, i);
        }

        if(mGameOver){
            int winner = mGame.checkForWinner();
            if (winner == 1) {
                mSoundPool.play(mSoundMap.get(3), 1, 1, 0, 0, 1);

                mInfoTextView.setTextColor(Color.rgb(0, 0, 200));
                mInfoTextView.setText(R.string.tie);

            } else if (winner == 2) {
                mSoundPool.play(mSoundMap.get(2), 1, 1, 0, 0, 1);
                mImageView.setVisibility(View.VISIBLE);
                mAnim.start();
                mInfoTextView.setTextColor(Color.rgb(0, 200, 0));
                mInfoTextView.setText(R.string.you_win);
            } else {
                mSoundPool.play(mSoundMap.get(4), 1, 1, 0, 0, 1);
                mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
                mInfoTextView.setText(R.string.machine_win);
            }
        }
        else if(moveCount > 0) {
            mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
            mInfoTextView.setText(R.string.your_turn);
        }

    }
}
