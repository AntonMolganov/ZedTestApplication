package com.example.zedtestapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class ProductionCalendar extends RelativeLayout {

    private ViewPager mViewPager;

    public ProductionCalendar(Context context) {
        super(context);
        init(null, 0);
    }

    public ProductionCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProductionCalendar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //inflate
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.productioncalendar, this, true);
        mViewPager = findViewById(R.id.pager);
    }

    public void setup(FragmentManager fm){
        mViewPager.setAdapter(new PagerAdapter(fm));
        Calendar c = Calendar.getInstance();
        mViewPager.setCurrentItem(c.get(Calendar.MONTH));
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private final static int MONTHS_TO_SHOW = 12;

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PageFragment();
            Bundle args = new Bundle();
            args.putInt(PageFragment.MONTH_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return MONTHS_TO_SHOW;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar c = new GregorianCalendar();
            c.set(Calendar.MONTH, position);
            Locale currentLocale = getResources().getConfiguration().locale;
            String monthDisplayName = new SimpleDateFormat("LLLL", currentLocale).format(c.getTime());
            return monthDisplayName.toLowerCase() + " " + c.get(Calendar.YEAR);
        }
    }

    public static class PageFragment extends Fragment {
        public static final String MONTH_NUMBER = "month";

        private int mMonth;
        private GridLayout mGrid;
        private TextView mText;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.productioncalendar_page, container, false);
            Bundle args = getArguments();
            mMonth = args.getInt(MONTH_NUMBER);
            mGrid = rootView.findViewById(R.id.grid);
            mText = rootView.findViewById(R.id.text);

            //do some preparations
            Calendar c = GregorianCalendar.getInstance();
            c.set(Calendar.MONTH, mMonth);
            int weekCount = c.getActualMaximum(Calendar.WEEK_OF_MONTH);
            int[][] calendarMatrix = new int[7][weekCount];
            int lastDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= lastDayOfMonth; i++){
                c.set(Calendar.DAY_OF_MONTH, i);
                int dayNum = c.get(Calendar.DAY_OF_WEEK);
                int dayIndex = (dayNum == 1) ? 6 : dayNum - 2; //make right mon to sun order
                int weekNum = c.get(Calendar.WEEK_OF_MONTH);
                int weekIndex = weekNum - 1;
                calendarMatrix[dayIndex][weekIndex] = i;
            }
            List<Integer> weekends = new LinkedList<>();
            for (int i=0; i < Data.YEAR_2017[0][mMonth].length;i++){
                weekends.add(Data.YEAR_2017[0][mMonth][i]);
            }
            List<Integer> shorts = new LinkedList<>();
            for (int i=0; i < Data.YEAR_2017[1][mMonth].length;i++){
                shorts.add(Data.YEAR_2017[1][mMonth][i]);
            }
            List<Integer> holydays = new LinkedList<>();
            for (int i=0; i < Data.YEAR_2017[2][mMonth].length;i++){
                holydays.add(Data.YEAR_2017[2][mMonth][i]);
            }


            mGrid.setColumnCount(7);
            //setup day captions
            for (int i = 0; i < 7; i++){
                String[] namesOfDays = DateFormatSymbols.getInstance().getShortWeekdays();//get locale specific names
                int index = (i < 6) ? i + 2 : 1; //make right mon to sun order
                CellView cell = new CellView(getContext());
                cell.setText(namesOfDays[index]);
                mGrid.addView(cell,i);
            }


            //setup days

            for (int i = 0; i < calendarMatrix.length * calendarMatrix[0].length; i++){
                CellView cell = new CellView(getContext());
                int weekIndex = i / 7;
                int dayIndex = i % 7;
                int dayNum = calendarMatrix[dayIndex][weekIndex];
                if (dayNum != 0) {
                    cell.setText(Integer.toString(dayNum));
                    if (shorts.contains(dayNum)) cell.setBackgroundColor(getResources().getColor(R.color.yellowDay));
                    else if (holydays.contains(dayNum) || weekends.contains(dayNum)) cell.setBackgroundColor(getResources().getColor(R.color.redDay));
                    else cell.setBackgroundColor(Color.TRANSPARENT);
                }else{
                    cell.setText("");
                    cell.setBackgroundColor(Color.TRANSPARENT);
                }
                mGrid.addView(cell,i+7);
            }

            //setup description
            StringBuilder description = new StringBuilder(getResources().getString(R.string.holydays_list) + ": ");
            for (int i = 0; i < Data.YEAR_2017[2][mMonth].length; i++) {
                description.append(Data.YEAR_2017[2][mMonth][i]).append(" ");
            }
            description.append("\n").append(getResources().getString(R.string.calendardays)).append(": ").append(lastDayOfMonth);
            description.append("\n").append(getResources().getString(R.string.workdays)).append(": ").append(lastDayOfMonth - weekends.size() - holydays.size());
            description.append("\n").append(getResources().getString(R.string.holydays)).append(": ").append(holydays.size() + weekends.size());
            description.append("\n").append(getResources().getString(R.string.hours_40)).append(": ").append(Data.YEAR_2017_HOURS[mMonth][0]);
            description.append("\n").append(getResources().getString(R.string.hours_36)).append(": ").append(Data.YEAR_2017_HOURS[mMonth][1]);
            description.append("\n").append(getResources().getString(R.string.hours_24)).append(": ").append(Data.YEAR_2017_HOURS[mMonth][2]);
            description.append("\n").append(mMonth / 3 + 1).append(" ").append(getResources().getString(R.string.quarter)).append(", ").append(mMonth / 6 + 1).append(" ").append(getResources().getString(R.string.halfyear)).append(", 2017 ").append(getResources().getString(R.string.year));


            mText.setText(description.toString());
            return rootView;
        }
    }

    public static class CellView extends TextView{
        public CellView(Context context) {
            super(context);
            init();
        }

        public CellView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CellView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init(){
            setGravity(Gravity.CENTER);
            setSingleLine();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int size = getResources().getDimensionPixelSize(R.dimen.calendar_cell_size);

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            if (widthMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                if ((widthMode == MeasureSpec.UNSPECIFIED) || (widthMode == MeasureSpec.AT_MOST && size < MeasureSpec.getSize(widthMeasureSpec)))
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
                if ((heightMode == MeasureSpec.UNSPECIFIED) || (heightMode == MeasureSpec.AT_MOST && size < MeasureSpec.getSize(heightMeasureSpec)))
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            } else if (widthMode != MeasureSpec.EXACTLY) {
                if ((widthMode == MeasureSpec.UNSPECIFIED) || (widthMode == MeasureSpec.AT_MOST && size < MeasureSpec.getSize(widthMeasureSpec)))
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            } else if (heightMode != MeasureSpec.EXACTLY) {
                if ((heightMode == MeasureSpec.UNSPECIFIED) || (heightMode == MeasureSpec.AT_MOST && size < MeasureSpec.getSize(heightMeasureSpec)))
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private static class Data{
        private final static int[][][] YEAR_2017 = {
                //weekends
                {
                        {14,15,21,22,28,29},//jan
                        {4,5,11,12,18,19,24,25,26},//feb
                        {4,5,11,12,18,19,25,26},//mar
                        {1,2,8,9,15,16,22,23,29,30},//apr
                        {6,7,8,13,14,20,21,27,28},//may
                        {3,4,10,11,17,18,24,25},//jun
                        {1,2,8,9,15,16,22,23,29,30},//jul
                        {5,6,12,13,19,20,26,27},//aug
                        {2,3,9,10,16,17,23,24,30},//sep
                        {1,7,8,14,15,21,22,28,29},//oct
                        {5,11,12,18,19,25,26},//nov
                        {2,3,9,10,16,17,23,24,30,31},//dec
                },
                //short days
                {
                        {},//jan
                        {22},//feb
                        {7},//mar
                        {},//apr
                        {},//may
                        {},//jun
                        {},//jul
                        {},//aug
                        {},//sep
                        {},//oct
                        {3},//nov
                        {},//dec
                },//holydays
                {
                        {1,2,3,4,5,6,7,8},//jan
                        {23},//feb
                        {8},//mar
                        {},//apr
                        {1,9},//may
                        {12},//jun
                        {},//jul
                        {},//aug
                        {},//sep
                        {},//oct
                        {4},//nov
                        {},//dec
                },
        };
        private final static float[][] YEAR_2017_HOURS = {
                //week hours
                        {136,122.4f,81.6f},//jan
                        {143,128.6f,85.4f},//feb
                        {175,157.4f,104.6f},//mar
                        {160,144,96},//apr
                        {160,144,96},//may
                        {168,151.2f,100.8f},//jun
                        {168,151.2f,100.8f},//jul
                        {184,165.6f,110.4f},//aug
                        {168,151.2f,100.8f},//sep
                        {176,158.4f,105.6f},//oct
                        {167,150.2f,99.8f},//nov
                        {168,151.2f,100.8f},//dec
        };
    }

}
