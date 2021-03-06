package org.sw24softwares.starkeverben;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.sw24softwares.starkeverben.Chart.XAxisValueFormatter;
import org.sw24softwares.starkeverben.Chart.XYMarkerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class ProgressFragment extends Fragment {
    private ExpandableListAdapter mListAdapter;
    private ExpandableListView mExpListView;
    private List<String> mListDataHeader;
    private HashMap<String, List<String>> mListDataChild;
    private DatabaseHelper mDatabaseHelper;

    private Vector<String> mDates = new Vector<>();
    private Vector<Integer> mScores = new Vector<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_progress, container, false);

        Context context = getActivity();

        mDatabaseHelper = new DatabaseHelper(context);

        mExpListView = view.findViewById(R.id.progress_list);

        prepareListData();
        prepareGraphData();

        mListAdapter = new ExpandableListAdapter(context, mListDataHeader, mListDataChild);
        mExpListView.setAdapter(mListAdapter);

        LineChart chart = view.findViewById(R.id.chart);
        chart.setNoDataText(getString(R.string.no_progress_data));

        if (!mScores.isEmpty()) {
            chart.setDescription(null);
            chart.setDrawGridBackground(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setMaxHighlightDistance(300);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.getLegend().setEnabled(false);

            if(Build.VERSION.SDK_INT >= 24) {
                XYMarkerView mv =
                    new XYMarkerView(context, new XAxisValueFormatter(mDates.toArray(new String[0])));
                mv.setChartView(chart);
                chart.setMarker(mv);
            }

            XAxis x = chart.getXAxis();
            x.setEnabled(true);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(false);
            x.setDrawLabels(false);
            x.setGranularity(1f);

            YAxis yLeft = chart.getAxisLeft();
            yLeft.setEnabled(true);
            yLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            yLeft.setDrawAxisLine(false);
            yLeft.setDrawGridLines(true);
            yLeft.setDrawLabels(false);
            yLeft.setXOffset(15);
            yLeft.setAxisMinimum(0f);
            yLeft.setAxisMaximum(100f);
            yLeft.setGranularityEnabled(true);

            chart.getAxisRight().setEnabled(false);

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < mScores.size(); i++)
                entries.add(new Entry(i, mScores.get(i)));

            LineDataSet dataSet = new LineDataSet(entries, "Scores");
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setCubicIntensity(0.2f);
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(2f);
            dataSet.setDrawValues(false);
            dataSet.setValueTextSize(12f);
            dataSet.setHighlightEnabled(true);
            dataSet.setColors(ContextCompat.getColor(context, R.color.colorAccent));
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.setVisibleXRangeMaximum(60);
            chart.moveViewToX(mDates.size());
        }

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TestActivity.class);
                    intent.putExtra("total", 0);
                    intent.putExtra("marks", new int[0]);
                    intent.putExtra("dialog", false);
                    startActivity(intent);
                }
            });
       if (Build.VERSION.SDK_INT >= 26)
           fab.setTooltipText(getString(R.string.test));

        return view;
    }

    private void prepareListData() {
        mListDataHeader = new ArrayList<>();
        mListDataChild = new HashMap<>();

        Cursor data = mDatabaseHelper.getListContents();
        data.moveToLast();
        data.moveToNext();

        while (data.moveToPrevious()) {
            String s[] = data.getString(1).split(" ");
            mListDataHeader.add(s[0] + " " + getString(R.string.at) + " " + s[1]
                                + " : " + s[2] + "%");
            List<String> details = new ArrayList<>();
            String results[] = data.getString(2).split(" ");
            for (String result : results) details.add(result + " / 5");
            mListDataChild.put(mListDataHeader.get(mListDataHeader.size() - 1), details);
        }
    }

    private void prepareGraphData() {
        Cursor data = mDatabaseHelper.getListContents();
        for (int i = 0; data.moveToNext(); i++) {
            String parts[] = data.getString(1).split(" ");

            mDates.addElement(parts[0] + " : " + parts[1]);
            mScores.addElement(Integer.parseInt(parts[2]));
        }
    }
}
