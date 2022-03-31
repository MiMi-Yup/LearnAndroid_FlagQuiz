package com.example.flagquiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_result_dialog.view.*

class ResultDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View = inflater.inflate(R.layout.fragment_result_dialog, container, false)

        view.txtResultDialog.text = getString(
            R.string.results,
            arguments?.getInt("totalGuesses"),
            (100.0 * arguments?.getInt("flagInQuiz")!! / arguments?.getInt("totalGuesses")!!)
        )

        view.btnPlayAgain.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                //TODO("Not yet implemented")
                var mainFragment: FragmentMain =
                    fragmentManager!!.findFragmentById(R.id.FragmentMain) as FragmentMain
                mainFragment.resetQuiz()
                dismiss()
            }
        })

        return view
    }

}