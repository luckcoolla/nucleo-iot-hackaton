package by.iot.nucleo.spectre.getyoursensors;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.HashMap;
import java.util.Map;

import by.iot.nucleo.spectre.getyoursensors.model.Board;
import by.iot.nucleo.spectre.getyoursensors.model.Sensor;
import by.iot.nucleo.spectre.getyoursensors.model.SensorType;
import by.iot.nucleo.spectre.getyoursensors.data.DataManager;
import by.iot.nucleo.spectre.getyoursensors.service.MqttService;

/**
 * A fragment representing a single Board detail screen.
 * This fragment is either contained in a {@link BoardListActivity}
 * in two-pane mode (on tablets) or a {@link BoardDetailActivity}
 * on handsets.
 */
public class BoardDetailFragment extends Fragment implements MqttService.MqttListener {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = BoardDetailFragment.class.getName();
    private Board mItem;
    private Map<String, LineChart> chartMap = new HashMap<>();
    private LinearLayout chartsWrap;

    public BoardDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DataManager.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getBoardName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.board_detail, container, false);
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.board_detail_tv)).setText("MQTT topic: " + mItem.getMqttTopic());
        }
        chartsWrap = (LinearLayout) rootView.findViewById(R.id.chartWrap);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MqttService.addListener(mItem.getMqttTopic(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MqttService.removeListener(mItem.getMqttTopic(), this);
    }

    @Override
    public void onMessageArrived(String topic, final Sensor[] mqttMessage) {
        chartsWrap.post(new Runnable() {
            @Override
            public void run() {
                for (Sensor sensor : mqttMessage) {
                    String key = sensor.getSensorName() + sensor.getSensorType().name();
                    LineChart lineChart = chartMap.get(key);
                    if (lineChart == null) {
                        lineChart = initNewChart(sensor.getSensorType(), chartMap.size());
                        if (lineChart != null) {
                            chartMap.put(key, lineChart);
                        }
                    }
                    if (lineChart != null) {
                        if (sensor.getSensorType().getValuesCount() == SensorType.SensorTypeValues.XY) {
                            addEntryToOneValueChart(lineChart, sensor);
                        } else {
                            //todo
                        }
                    }
                }
            }
        });
    }

    private LineChart initNewChart(SensorType sensorType, int initializedChartsCount) {
//      FIXME dynamic inflation doesn't work
//        LayoutInflater inflater = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View inflatedLayout= inflater.inflate(R.layout.sensor_item_layout, chartsWrap, false);
//        LineChart chart1 = (LineChart) inflatedLayout.findViewById(R.id.chart_item);
//        chartsWrap.addView(inflatedLayout);
        LineChart chart = null;
        //if initializedChartsCount is 0 then get the first one from list
        for (int i = 0; i < chartsWrap.getChildCount(); i++) {
            if ((initializedChartsCount == 0 || chartsWrap.getChildAt(i).getVisibility() == View.GONE)
                    && chartsWrap.getChildAt(i) instanceof LineChart) {
                chartsWrap.getChildAt(i).setVisibility(View.VISIBLE);
                chart = (LineChart) chartsWrap.getChildAt(i);
                break;
            }
        }
        if (chart != null) {
            if (sensorType.getValuesCount() == SensorType.SensorTypeValues.XY) {
                initChartOneValue(chart, sensorType);
            } else {
                //todo
            }
        } else {
            Log.e(TAG, "initNewChart(..) failed: there are no available charts");
        }
        return chart;
    }

    private void addEntryToOneValueChart(LineChart chart, Sensor sensor) {
        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createOneValueSet(sensor.getSensorType(), sensor.getSensorName());
                data.addDataSet(set);
            }

            float newX = set.getEntryCount();
            data.addEntry(new Entry(newX, sensor.getValue()), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            if (chart.isFullyZoomedOut()) {
                chart.moveViewToX(newX);
            }

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        } else {
            Log.w(TAG, "addEntryToOneValueChart(..) failed: ");
        }
    }


    private LineDataSet createOneValueSet(@NonNull SensorType sensorType, String sensorName) {
        //one value
        int color = sensorType.getColors()[0];
        String lineName = "SensorType : " + sensorType.name() + " "
                + ((sensorName != null && !sensorName.isEmpty())? "SensorName : " + sensorName : "");
        LineDataSet set = new LineDataSet(null, lineName);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(color);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(color);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    private void initChartOneValue(LineChart chart, SensorType sensorType) {
        //one value
        int color = sensorType.getColors()[0];

        chart.setNoDataText("Waiting for the sensor values");
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        //chart.setPinchZoom(true);
        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);
        LineData data = new LineData();
        data.setValueTextColor(color);
        // add empty data
        chart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(color);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(sensorType.getMaxValue());
        leftAxis.setAxisMinimum(sensorType.getMinValue());
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }
}
