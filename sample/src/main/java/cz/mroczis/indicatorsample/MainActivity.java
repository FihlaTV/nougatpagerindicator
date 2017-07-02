package cz.mroczis.indicatorsample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import cz.mroczis.nougatpagerindicator.PagerIndicator;

/**
 * Activity containing two instances of {@link PagerIndicator} and some buttons which helps
 * us interact with it.
 * Created by Michal on 29.06.17.
 */
public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_VIEW_PAGER_POSITION = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupAutomaticIndicator();
        setupManualIndicator();
    }

    /**
     * Simple usage of {@link PagerIndicator} when it is not used with {@link ViewPager}
     */
    private void setupManualIndicator() {
        final PagerIndicator indicator = findViewById(R.id.indicator_manual);
        final Button buttonLeft = findViewById(R.id.move_left_manual);
        final Button buttonRight = findViewById(R.id.move_right_manual);
        final Button buttonRandom = findViewById(R.id.move_random_manual);

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = indicator.getCurrentItem() - 1;
                if (newPosition >= 0) {
                    indicator.setCurrentItem(newPosition, true);
                }
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = indicator.getCurrentItem() + 1;
                if (newPosition < indicator.getDotsCount()) {
                    indicator.setCurrentItem(newPosition, true);
                }
            }
        });

        buttonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = indicator.getCurrentItem();
                int next;

                do {
                    next = (int) (Math.random() * (indicator.getDotsCount() - 1));
                    indicator.setCurrentItem(next, true);
                } while (current == next);
            }
        });
    }

    /**
     * Sample usage of {@link PagerIndicator} with {@link ViewPager}
     */
    private void setupAutomaticIndicator() {
        final PagerIndicator indicator = findViewById(R.id.indicator_automatic);
        final ViewPager pager = findViewById(R.id.pager);

        pager.setAdapter(new SampleAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(DEFAULT_VIEW_PAGER_POSITION);

        indicator.setupWithViewPager(pager);

        final Button buttonLeft = findViewById(R.id.move_left_automatic);
        final Button buttonRight = findViewById(R.id.move_right_automatic);
        final Button buttonRandom = findViewById(R.id.move_random_automatic);

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = pager.getCurrentItem() - 1;
                if (newPosition >= 0) {
                    pager.setCurrentItem(newPosition, true);
                }
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = pager.getCurrentItem() + 1;
                if (newPosition < pager.getAdapter().getCount() ) {
                    pager.setCurrentItem(newPosition, true);
                }
            }
        });

        buttonRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = pager.getCurrentItem();
                int next;

                do {
                    next = (int) (Math.random() * (pager.getAdapter().getCount() - 1));
                    pager.setCurrentItem(next, true);
                } while (current == next);
            }
        });
    }
}
