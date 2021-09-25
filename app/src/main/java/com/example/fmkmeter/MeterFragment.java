package com.example.fmkmeter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.fmkmeter.databinding.FragmentMeterBinding;
import com.example.fmkmeter.utils.FileUtils;
import com.example.fmkmeter.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MeterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public /*abstract*/ class MeterFragment<V extends MeterContractor.View, P extends MeterContractor.Presenter<V>> extends Fragment
        implements MeterContractor.View,
        CalculateAsyncTask.CalculateAsyncTaskListener,
        CalculateAsyncTaskNew.CalculateAsyncTaskNewListener,
        SaveFileDialogFragment.SaveFileDialogListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "MeterFragment";
    protected MeterContractor.Presenter presenter;
    private boolean isIzmStart = false;
    private FragmentMeterBinding binding;
    private OnFragmentInteractionListener mListener;
    ArrayAdapter<String> adapter;
    private CountDownTimer timerStart = null;
    private CountDownTimer timerMeasurment = null;
    private List<String> spinnerListDevices = new ArrayList<String>();
    private PendingIntent mPermissionIntent;
    MeterViewModel viewModel;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public MeterFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MeterFragment newInstance(/*String param1, String param2*/) {
        MeterFragment fragment = new MeterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int izm;
        if (getArguments() != null) {
            izm = getArguments().getInt("isStartIzm");
            Log.d("onCreate", "" + izm);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meter, container, false);
        View view = ((ViewDataBinding) binding).getRoot();
        initListeners();
        viewModel =
                ViewModelProviders.of(getActivity()).get(MeterViewModel.class);
        /*if (viewModel.getPresenter() == null) {
            viewModel.setPresenter(new MeterPresenter());
        }*/
        presenter = viewModel.getPresenter();
        presenter.attachLifecycle(getLifecycle());
        //presenter.attachView((V) this);
        presenter.setLifecycleOwner(/*viewModel.getRepository(), */this);
        timerStart = viewModel.getStartTimer(SharedPreferenceUtils.getDelayedStartTime(getContext()));
        timerMeasurment = viewModel.getMeasurementTimer(SharedPreferenceUtils.getMeasurmentTime(getContext()));
        spinnerListDevices = presenter.getListDevices();
        setSpinners();
        setHasOptionsMenu(true);
        int isStartIzm = MeterFragmentArgs.fromBundle(getArguments()).getIsStartIzm();
        if (isStartIzm == 1)
            presenter.startIzmOnClick();
        //return inflater.inflate(R.layout.fragment_meter, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getTimerStartData().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long s) {
                if(viewModel.timerStartIsFinish){
                    binding.layoutTimerStart.setVisibility(View.GONE);
                    if(viewModel.isClickStart) {
                        viewModel.isClickStart=false;
                        try {
                            presenter.startIzmOnClick();
                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                        if (SharedPreferenceUtils.getIsAutoMeasurment(getContext()) && viewModel.timerMeasurmentIsFinish) {
                            binding.layoutTimerMeasurements.setVisibility(View.VISIBLE);
                            timerMeasurment.start();
                        }
                    }
                    viewModel.isClickStart=false;
                } else{
                    binding.layoutTimerStart.setVisibility(View.VISIBLE);
                    binding.tvTimerStart.setText(s+" c.");
                }
            }
        });
        viewModel.getTimerMeasurementData().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long s) {
                if(viewModel.timerMeasurmentIsFinish){
                    binding.layoutTimerMeasurements.setVisibility(View.INVISIBLE);
                    if(isIzmStart)
                        presenter.finishIzmOnClick(true);
                } else{
                    binding.layoutTimerMeasurements.setVisibility(View.VISIBLE);
                    binding.tvTimerMeasurements.setText(s+" c.");
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_fragment_settings:
                //NavHostFragment navHostFragment =  NavHostFragment.findNavController(this);
                NavController navController = NavHostFragment.findNavController(this);
                NavigationUI.onNavDestinationSelected(item, navController);
                //NavController navController = NavHostFragment.findNavController(R.id.nav_host_fragment);
                //item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
                break;
            case R.id.menu_info:
                presenter.getInfoDialog().show(getFragmentManager(), TAG);
                break;
            case R.id.menu_refresh:
                adapter.clear();
                presenter.refreshOnClick();
                adapter.notifyDataSetChanged();
                break;
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }*/

    @Override
    public void onStart() {
        presenter.attachView((V) this);
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.detachLifecycle(getLifecycle());
        presenter.detachView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void initListeners() {
        binding.btnResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Navigation.findNavController(v).navigate(R.id.action_meterFragment_to_chartFragment);
            }
        });
        binding.btnStartIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIzmOnClick();
            }
        });
        binding.btnFinishIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishIzmOnClick();
            }
        });
        binding.btnSingleIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.singlIzmOnClick();
                //SingleIzmNew();
            }
        });
        /*binding.btnTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.finishIzmOnClickTest();
            }
        });*/

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        getActivity().getApplicationContext().registerReceiver(mUsbPlugEvents, filter);

        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().getApplicationContext().registerReceiver(mUsbReceiver, filter);
    /*   UsbManager mUsbManager = (UsbManager)getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);

...
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().getApplicationContext().registerReceiver(mUsbPlugEvents, filter);*/
        //registerReceiver(mUsbReceiver, filter);
    }

    private void startIzmOnClick() {
        if(presenter.deviceIsOpened()) {
        viewModel.isClickStart = true;
        viewModel.isAutoBegin = true;
        //while(viewModel.isAutoBegin){
            viewModel.isAutoBegin = SharedPreferenceUtils.getIsAutoMeasurment(getContext());
            if(viewModel.isAutoBegin && !viewModel.isAutoSaveDialogShown){
                SaveFileDialogFragment.newInstance(this, true).show(getParentFragmentManager(), TAG);
                viewModel.isAutoSaveDialogShown = true;
            } else {
                if (SharedPreferenceUtils.getIsDelayedStart(getContext())) {
                    binding.layoutTimerStart.setVisibility(View.VISIBLE);
                    timerStart.start();
                    izmIsStart(true);
                } else {
                    presenter.startIzmOnClick();
                    if (SharedPreferenceUtils.getIsAutoMeasurment(getContext()) && viewModel.timerMeasurmentIsFinish) {
                        binding.layoutTimerMeasurements.setVisibility(View.VISIBLE);
                        timerMeasurment.start();
                        viewModel.isClickStart = false;
                    }
                }
            }
        //}
        } else showToast(getString(R.string.msg_device_not_open));
    }

    private void finishIzmOnClick(){
        viewModel.isAutoBegin = false;
        viewModel.isAutoSaveDialogShown = false;
        viewModel.isClickStart = false;
        if(SharedPreferenceUtils.getIsDelayedStart(getContext())){
            if(!viewModel.timerStartIsFinish) {
                timerStart.cancel();
                timerStart.onFinish();
                izmIsStart(false);
                return;
            }
        }
        if (SharedPreferenceUtils.getIsAutoMeasurment(getContext())) {
            timerMeasurment.cancel();
            timerMeasurment.onFinish();
        } else presenter.finishIzmOnClick(false);
    }

    private void setSpinners() {
        adapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerListDevices);
        binding.spnDevices.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spnDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.spnDevicesItemOnClick(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Log.v("onNothingSelected", parent.toString());
            }
        });

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.array_si, android.R.layout.simple_spinner_dropdown_item);
        binding.spnSi.setAdapter(adapter1);
        binding.spnSi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("onItemSelected", view.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), InfoDialogFragment.TAG);
    }

    @Override
    public void updateCounter(int cnt) {
        binding.tvCountByteAll.setText("byteAll=" + cnt);
    }

    @Override
    public void updateResult(int cnt) {
        binding.tvCountByteResult.setText("byteRes=" + cnt);
    }

    @Override
    public void showProgressBar(boolean tf) {
        if (tf)
            binding.progressBar.setVisibility(View.VISIBLE);
        else
            binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void selectSpnDevice(int index) {
        binding.spnDevices.setSelection(index);
        binding.spnDevices.getSelectedItem();
    }

    @Override
    public void goToResult() {
        if(viewModel.isAutoBegin){
            presenter.startCalculate(this, this);
        } else Navigation.findNavController(binding.btnResult).navigate(R.id.action_meterFragment_to_chartFragment);
    }

    @Override
    public void izmIsStart(boolean isStart) {
        isIzmStart = isStart;
        if (isStart) {
            binding.btnStartIzm.setVisibility(View.GONE);
            binding.btnFinishIzm.setVisibility(View.VISIBLE);
        } else {
            binding.btnStartIzm.setVisibility(View.VISIBLE);
            binding.btnFinishIzm.setVisibility(View.GONE);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                //Device was attached, ask for permission
                UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                usbManager.requestPermission(device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                //Disconnected
                //UsbMonitorConnection.disconnect();
            }

          /* if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                try {
                    //spinnerListDevices = presenter.getListDevices();
                    //presenter.refreshOnClick();
                    //adapter.notifyDataSetChanged();
                    UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    usbManager.requestPermission(device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //binding.tvRead.setText(s);
                        //error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    //InitDeviceList();
                    //ConnectDevice(openIndex);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //binding.tvRead.setText(s);
                        //error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }*/
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        && device != null) {
                    presenter.refreshOnClick();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "permission succes for device " + device);
                    //getDeviceStatus(device);
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
            }
        }
    };

    @Override
    public void onPostCalculateConcluded(List<Signal> outData, List<Signal> outIntegrateFirstData, List<Signal> outIntegrateSecondData, float minMaxValue, float minMaxIntegrateValue, float min, float max, float average) {
        if (outData != null && outData.size() > 0) {
            String fileName = viewModel.getAutoSaveFileName();
            showToast(FileUtils.saveFile(outData, outIntegrateFirstData, outIntegrateSecondData, getContext(), fileName));
        } else showToast(getString(R.string.msg_data_not_found));
        if(viewModel.isAutoBegin)
            startIzmOnClick();
    }

    @Override
    public void onPostCalculateNewConcluded(List<Signal> outData, List<Signal> outIntegrateFirstData, List<Signal> outIntegrateSecondData, float minMaxValue, float minMaxIntegrateValue, int[] indexesMinMax) {
        if (outData != null && outData.size() > 0) {
            String fileName = viewModel.getAutoSaveFileName();
            showToast(FileUtils.saveFile(outData, outIntegrateFirstData, outIntegrateSecondData, getContext(), fileName));
        } else showToast(getString(R.string.msg_data_not_found));
        if(viewModel.isAutoBegin)
            startIzmOnClick();
    }

    @Override
    public void onSaveDialogPositiveClick(String uchastok, String put, String nOpora, String nIzm) {
        viewModel.setAutoSaveParam(uchastok, put, nOpora);
        startIzmOnClick();
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
