package com.ifenglian.rocklet.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ifenglian.rocklet.R;


public class No_resultFragment extends Fragment {

	private ReAddClick listener;

	public No_resultFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_prompt, container, false);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button reAddBtn = (Button) getActivity().findViewById(R.id.prompt_fragment_re_add);
		reAddBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.click();
			}
		});
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof ReAddClick) {
			listener = (ReAddClick) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement ReAddClick");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public interface ReAddClick {
		void click();
	}
}
