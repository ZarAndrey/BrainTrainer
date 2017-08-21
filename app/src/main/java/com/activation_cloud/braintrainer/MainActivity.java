package com.activation_cloud.braintrainer;

import android.content.pm.ActivityInfo;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Semaphore;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, Runnable{

    Button gobuttom;
    Button bm1;
    Button bm2;
    Button bm3;
    Button bm4;
    ConstraintLayout lt1;
    ConstraintLayout lt2;
    GridLayout grid;
    TextView resView,timeView,pointView,levelView,sampleView;

    Thread th;
    static Semaphore mutex = new Semaphore(1);

    int timer_start = 30;
    int num_question = 0;
    int num_answer = 0;
    int level = 1;
    int locationOfCorrectAnswer;
    ArrayList<Integer> answers = new ArrayList<Integer>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gobuttom = (Button)findViewById(R.id.gobuttom);
        gobuttom.setOnClickListener(this);
        bm1 = (Button)findViewById(R.id.button1);
        bm1.setOnClickListener(this);
        bm2 = (Button)findViewById(R.id.button2);
        bm2.setOnClickListener(this);
        bm3 = (Button)findViewById(R.id.button3);
        bm3.setOnClickListener(this);
        bm4 = (Button)findViewById(R.id.button4);
        bm4.setOnClickListener(this);

        lt1 = (ConstraintLayout)findViewById(R.id.constraintLayout);
        lt2 = (ConstraintLayout)findViewById(R.id.constraintLayout2);
        grid = (GridLayout)findViewById(R.id.gridLayout);
        resView = (TextView)findViewById(R.id.resView);
        timeView = (TextView)findViewById(R.id.timeView);
        pointView = (TextView)findViewById(R.id.pointView);
        levelView = (TextView)findViewById(R.id.levelViev);
        sampleView = (TextView)findViewById(R.id.sampleView);
        setVisible(View.INVISIBLE);

    }

    @Override
    public void onClick(View view) {
        //Log.i("INFO","OnClick");
        if(view == null)
            return;
        //Log.i("INFO","OnClick:"+Integer.toString(view.getId()));
        switch (view.getId())
        {
            case R.id.gobuttom:
                gobuttom.setVisibility(View.INVISIBLE);
                resView.setVisibility(View.INVISIBLE);
                setVisible(View.VISIBLE);
                UpdateData(30,1,0,0);
                UpdateView(timer_start,level,num_question,num_answer);
                generateQuestion();
                th = new Thread(this);
                th.start();
                break;
            case R.id.button1:
            case R.id.button2:
            case R.id.button3:
            case R.id.button4:
                Button tmp = (Button)view;
                //Log.i("INFO",tmp.getText().toString());
                //Log.i("INFO",Integer.toString(answers.get(locationOfCorrectAnswer)));
                //if(tmp.getText().equals(Integer.toString(answers.get(locationOfCorrectAnswer))))
                if(answers.get(locationOfCorrectAnswer) == Integer.parseInt(tmp.getText().toString()))
                {
                    num_answer++;
                    Toast.makeText(this,"Correct!",Toast.LENGTH_SHORT).show();
                    UpdateTimer(level);

                }
                else {
                    Toast.makeText(this,"Incorrect!",Toast.LENGTH_SHORT).show();
                    UpdateTimer(-1);
                }

                num_question++;

                UpdateView(timer_start,level,num_question,num_answer);
                generateQuestion();

                break;
        }
    }

    @Override
    public void run() {

        while(timer_start > 0)
        {
            UpdateTimer(-1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        StopGame();

    }

    private void setVisible(int visible)
    {
        lt1.setVisibility(visible);
        lt2.setVisibility(visible);
        grid.setVisibility(visible);
    }

    public void UpdateView(int start_time, int level, int question, int answer)
    {
        levelView.setText("Level: "+ Integer.toString(level));
        pointView.setText(Integer.toString(answer)+"/"+Integer.toString(question));
        String str = timeToString(start_time);
        timeView.setText(str);
    }

    protected void UpdateData(int _time, int _level, int _question, int _answer)
    {
        timer_start = _time;
        level = _level;
        num_question = _question;
        num_answer = _answer;

    }

    private String timeToString(int seconds)
    {
        int min = (int)seconds/60;
        int sec = seconds%60;
        String tmp = Integer.toString(sec);
        if(sec < 9)
        {
            tmp = "0"+tmp;
        }
        return Integer.toString(min)+":"+tmp+"s";
    }

    public void StopGame()
    {
        try {
            mutex.acquire();
            try
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        resView.setVisibility(View.VISIBLE);
                        resView.setText("Game finished!\nYour Result:"+ Integer.toString(num_answer)+"/"+Integer.toString(num_question));
                        setVisible(View.INVISIBLE);
                        gobuttom.setText("Again?");
                        gobuttom.setVisibility(View.VISIBLE);

                    }
                });


            }finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void UpdateTimer(int tic)
    {
        try {
            mutex.acquire();
            try
            {
                timer_start += tic;
                timer_start = timer_start >0 ? timer_start:0;
                //Log.i("INFO",Integer.toString(timer_start));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        UpdateView(timer_start,level,num_question,num_answer);
                    }
                });

            }finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void UpdateButton(ArrayList<Integer> l )
    {
        bm1.setText(Integer.toString(l.get(0)));
        bm2.setText(Integer.toString(l.get(1)));
        bm3.setText(Integer.toString(l.get(2)));
        bm4.setText(Integer.toString(l.get(3)));
    }

    public void EnableButton(boolean b)
    {
        bm1.setEnabled(b);
        bm2.setEnabled(b);
        bm3.setEnabled(b);
        bm4.setEnabled(b);
    }

    public void generateQuestion()
    {
        EnableButton(false);
        Random rand = new Random();
        int a,b,incorrectAnswer;
        answers.clear();
        HashSet<Integer> tmp_set =  new HashSet<Integer>();
        if(num_answer>= 10 && num_answer%10 == 0)
            level++;
        //Log.i("INFO","in lv1");
        a = 10*(level-1)+rand.nextInt(10*level);
		b = 10*(level-1)+rand.nextInt(10*level);
		sampleView.setText(Integer.toString(a)+"+"+Integer.toString(b));
		locationOfCorrectAnswer = rand.nextInt(4);
		for(int i = 0; i < 4; i++) {
            //Log.i("INFO","in FOR:"+Integer.toString(i));
            if (i == locationOfCorrectAnswer) {
                answers.add(a + b);
            } else {
                boolean find = false;
                incorrectAnswer = 0;
                while (!find) {
                    Random tmp_rand = new Random();
                    int r = Math.abs(a + b) < 10 ? Math.abs(a + b) + 5 : Math.abs(a + b);

                    incorrectAnswer = tmp_rand.nextInt(r) + a + b;
                    //Log.i("INFO","Find:" + Integer.toString(incorrectAnswer));
                    if (incorrectAnswer != a + b) {
                        if (tmp_set.add(incorrectAnswer)) {
                            find = true;
                            //Log.i("INFO",Integer.toString(tmp_set.size()));
                        }
                    }
                }
                answers.add(incorrectAnswer);
            }
        }

		UpdateButton(answers);

        EnableButton(true);

    }

}
