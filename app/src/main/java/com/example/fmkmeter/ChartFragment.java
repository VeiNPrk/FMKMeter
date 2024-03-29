package com.example.fmkmeter;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.fmkmeter.databinding.FragmentChartBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment<V extends ChartContractor.View, P extends ChartContractor.Presenter<V>> extends Fragment
    implements ChartContractor.View {

    public static final String TAG="ChartFragment";
    private static final int MIN = 500;
    private static final int MAX = 10000;
    private static final int MINDelta = -16000;
    private static final int MAXDelta = 16000;
    private static final int STEP = 100;
    private static final int STEPDelta = 200;
    private static final int startDeltaProgress = 50;
    protected ChartContractor.Presenter presenter;
    private FragmentChartBinding binding;
    private static boolean isNewInstance = false;
    ChartFragmentDirections.ActionChartFragmentToMeterFragment action = ChartFragmentDirections.actionChartFragmentToMeterFragment();

    //List<String> spinnerListDevices = new ArrayList<String>();
    private OnFragmentInteractionListener mListener;

    public ChartFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(/*String param1, String param2*/) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        /*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        isNewInstance = true;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            /*mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);*/
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false);
        final View view = ((ViewDataBinding) binding).getRoot();
        initListeners();
        ChartViewModel viewModel =
        ViewModelProviders.of(getActivity()).get(ChartViewModel.class);
        if (viewModel.getPresenter() == null) {
            viewModel.setPresenter(new ChartPresenter());
        }
        presenter = viewModel.getPresenter();
        presenter.attachLifecycle(getLifecycle());
        presenter.attachView((V) this);
        presenter.setRepository(viewModel.getRepository(), this, isNewInstance);
        if(isNewInstance)
            isNewInstance=false;
        //spinnerListDevices = presenter.getListDevices();
        setHasOptionsMenu(false);
        binding.tvStep.setText(""+MIN);
        presenter.setSeekBarStep(0, MIN);
        presenter.setSeekBarDelta(startDeltaProgress,MINDelta);
        action.setIsStartIzm(0);
        //presenter.loadDataOnClick();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ChartFragmentDirections.ActionChartFragmentToMeterFragment action = ChartFragmentDirections.actionChartFragmentToMeterFragment();
                action.setIsStartIzm(0);
                Navigation.findNavController(view).navigate(action);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        presenter.detachLifecycle(getLifecycle());
        presenter.detachView();
        super.onStop();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void initListeners(){
        //binding.tvRead.setMovementMethod(new ScrollingMovementMethod());
        binding.btnNextSet.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                presenter.nextSetOnClick();
            }
        });
        binding.btnPrewSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.prewSetOnClick();
            }
        });
        binding.btnToIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.setIsStartIzm(1);
                Navigation.findNavController(v).navigate(action);
            }
        });
        //binding.sbStepChart.setProgress(0);
        binding.sbStepChart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                presenter.loadDataOnClick();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG,"sbStepChart onProgressChanged");
                double value = Math.round((progress * (MAX - MIN)) / 100);
                int displayValue = (((int) value + MIN) / STEP) * STEP;
                binding.tvStep.setText(""+displayValue);
                Log.d(TAG,"tvStep text "+displayValue);
                presenter.setSeekBarStep(progress, displayValue);
            }
        });
        //binding.sbDeltaChart.setProgress(0);
        binding.sbDeltaChart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"sbDeltaChart onStopTrackingTouch");
                presenter.loadDataOnClick();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG,"sbDeltaChart onProgressChanged");
                double value = Math.round((progress * (MAXDelta - MINDelta)) / 100);
                int displayValue = (((int) value + MINDelta) / STEPDelta) * STEPDelta;
                binding.tvStep.setText(""+displayValue);
                Log.d(TAG,"tvStep text "+displayValue);
                presenter.setSeekBarDelta(progress, displayValue);
            }

        });
        binding.switchResult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG,"switchResult onCheckedChanged");
                presenter.setSwitchChartValue(isChecked);
                binding.sbDeltaChart.setProgress(startDeltaProgress);
                binding.sbStepChart.setProgress(0);
                if(isChecked) {
                    binding.sbDeltaChart.setVisibility(View.VISIBLE);
                    binding.sbStepChart.setVisibility(View.GONE);
                    binding.tvSbTittle.setText(getString(R.string.tv_tittle_sb_delta));
                    binding.sbDeltaChart.setProgress(10);
                    binding.sbDeltaChart.setProgress(startDeltaProgress);
                }
                else{
                    binding.sbDeltaChart.setVisibility(View.GONE);
                    binding.sbStepChart.setVisibility(View.VISIBLE);
                    binding.tvSbTittle.setText(getString(R.string.tv_tittle_sb_step));
                    binding.sbStepChart.setProgress(10);
                    binding.sbStepChart.setProgress(0);
                }
                //presenter.loadDataOnClick();
            }
        });
        binding.switchResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"switchResult onClick");
                presenter.loadDataOnClick();
            }
        });
    }

    @Override
    public void showProgressBar(boolean tf) {
        if(tf){
            binding.chart.setVisibility(View.GONE);
            binding.progressContainer.setVisibility(View.VISIBLE);
        }
        else{
            binding.chart.setVisibility(View.VISIBLE);
            binding.progressContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNextButton(boolean tf) {
        binding.btnNextSet.setEnabled(tf);
    }

    @Override
    public void showPrewButton(boolean tf) {
        binding.btnPrewSet.setEnabled(tf);
    }

    @Override
    public void showChartForm(boolean tf) {
        if(tf){
            binding.chart.setVisibility(View.VISIBLE);
        }
        else{
            binding.chart.setVisibility(View.GONE);
        }
    }

    @Override
    public void setChart(LineData data) {
        binding.chart.clear();
        binding.chart.setData(data);
        binding.chart.invalidate();
    }

    @Override
    public void setSizeData(int countData) {
        binding.tvSizeData.setText(""+countData);
    }

    @Override
    public void setTVIndexChart(int indexChart, int lastIndexChart) {
        binding.tvIndexChart.setText(++indexChart+" из "+ ++lastIndexChart);
    }

    @Override
    public void setMinMax(int minMax) {
        binding.tvMinMax.setText(""+minMax);
    }

    @Override
    public boolean getSwitchedResult() {
        return binding.switchResult.isChecked();
        //return false;
    }

    @Override
    public void showMessageForm(boolean tf, String msg) {
        if(tf){
            binding.tvMessageForm.setVisibility(View.VISIBLE);
            binding.tvMessageForm.setText(msg);
        }
        else{
            binding.tvMessageForm.setVisibility(View.GONE);
        }
    }

    @Override
    public void showToastMessage(String msg) {
        //Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSeekBarStep(int progress) {
        Log.d(TAG,"setSeekBarStep "+progress);
        binding.sbStepChart.setProgress(progress*2);
        binding.sbStepChart.setProgress(progress);
        //binding.sbStepChart.
    }

    @Override
    public void setSwitch(boolean isChecked) {
        binding.switchResult.setChecked(isChecked);
    }

    @Override
    public LineChart getChart() {
        return binding.chart;
    }

    @Override
    public void setSeekBarDelta(int progress) {
        Log.d(TAG,"setSeekBarDelta "+progress);
        binding.sbDeltaChart.setProgress(progress*2);
        binding.sbDeltaChart.setProgress(progress);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
