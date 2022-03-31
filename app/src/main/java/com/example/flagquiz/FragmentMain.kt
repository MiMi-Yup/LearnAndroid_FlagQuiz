package com.example.flagquiz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.util.*

class FragmentMain : Fragment() {
    private val TAG: String = "FlagQuiz"
    private val FLAGS_IN_QUIZ: Int = 10

    private lateinit var fileNameList: ArrayList<String>
    private lateinit var quizCountriesList: ArrayList<String>
    private lateinit var regionSet: Set<String>
    private lateinit var correctAnswer: String
    private var totalGuesses: Int = 0
    private var correctAnswers: Int = 0
    private var guessRows: Int = 0
    private lateinit var random: SecureRandom
    private lateinit var handler: Handler
    private lateinit var shakeAnimation: Animation
    private lateinit var guessLinearLayouts: Array<LinearLayout>
    private lateinit var quizLinearLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        var view: View = inflater.inflate(R.layout.fragment_main, container, false)

        fileNameList = ArrayList<String>()
        quizCountriesList = ArrayList<String>()
        random = SecureRandom()
        handler = Handler()
        shakeAnimation = AnimationUtils.loadAnimation(activity, R.anim.incorrect_shake)
        shakeAnimation.repeatCount = 3
        quizLinearLayout = view.findViewById<LinearLayout>(R.id.quizLinearLayout)
        guessLinearLayouts = arrayOf(
            view.findViewById(R.id.row1LinearLayout),
            view.findViewById(R.id.row2LinearLayout),
            view.findViewById(R.id.row3LinearLayout),
            view.findViewById(R.id.row4LinearLayout)
        )

        for (row in guessLinearLayouts) {
            for (index in 0..(row.childCount - 1)) {
                var button: Button = row.getChildAt(index) as Button
                button.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        //TODO("Not yet implemented")
                        val guessBtn: Button = view as Button
                        val guess: String = guessBtn.text.toString()
                        val answer: String = getCountryName(correctAnswer)
                        totalGuesses++

                        if (guess.equals(answer)) {
                            correctAnswers++

                            txtAnswer.text = answer + "!"
                            txtAnswer.setTextColor(
                                resources.getColor(
                                    R.color.correct_answer,
                                    context?.theme
                                )
                            )
                            txtAnswer.visibility = View.VISIBLE
                            disableButton()

                            if (correctAnswers == FLAGS_IN_QUIZ) {
                                val quizResult: DialogFragment = ResultDialogFragment()
                                val result: Bundle = Bundle()

                                result.putInt("totalGuesses", totalGuesses)
                                result.putInt("flagInQuiz", FLAGS_IN_QUIZ)

                                quizResult.arguments = result
                                quizResult.show(fragmentManager!!, "quiz dialog")
                            } else {
                                handler.postDelayed(object : Runnable {
                                    override fun run() {
                                        //TODO("Not yet implemented")
                                        animate(true)
                                    }
                                }, 2000)
                            }
                        } else {
                            imgFlag.startAnimation(shakeAnimation)

                            txtAnswer.setText(R.string.incorrect_answer)
                            txtAnswer.setTextColor(
                                resources.getColor(
                                    R.color.incorrect_answer,
                                    context?.theme
                                )
                            )
                            txtAnswer.visibility = View.VISIBLE
                            guessBtn.isEnabled = false
                        }
                    }
                })
            }
        }

        val txtQuestionNumber = view.findViewById<TextView>(R.id.txtQuestionNumber)
        txtQuestionNumber.text = resources.getString(R.string.question, 1, FLAGS_IN_QUIZ)

        return view
    }

    private fun disableButton(): Unit {
        for (row in (0..guessRows - 1)) {
            val guessRow: LinearLayout = guessLinearLayouts[row]
            for (index in 0..(guessRow.childCount - 1)) {
                guessRow.getChildAt(index).isEnabled = false
            }
        }
    }

    fun updateGuessRows(sharedPreferences: SharedPreferences): Unit {
        var choices: String? = sharedPreferences.getString(MainActivity.CHOICES, null)
        guessRows = choices!!.toInt() / 2

        for (row in guessLinearLayouts) {
            row.visibility = View.GONE
        }

        for (index in 0..(guessRows - 1)) {
            guessLinearLayouts[index].visibility = View.VISIBLE
        }
    }

    fun updateRegions(sharedPreferences: SharedPreferences): Unit {
        regionSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null)!!
    }

    fun resetQuiz(): Unit {
        val assets: AssetManager = requireActivity().assets
        fileNameList.clear()

        try {
            for (region in regionSet) {
                val paths: Array<String>? = assets.list(region)
                for (path in paths!!) {
                    fileNameList.add(path.replace(".png", ""))
                }
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Error loading image file names", exception)
        }

        correctAnswers = 0
        totalGuesses = 0
        quizCountriesList.clear()

        var flagCounter: Int = 1
        var numberOfFlags = fileNameList.size

        while (flagCounter <= FLAGS_IN_QUIZ) {
            var randomIndex: Int = random.nextInt(numberOfFlags)

            var filename: String = fileNameList[randomIndex]

            if (!quizCountriesList.contains(filename)) {
                quizCountriesList.add(filename)
                flagCounter++
            }
        }
        loadNextFlag()
    }

    private fun loadNextFlag(): Unit {
        val nextImage: String = quizCountriesList.removeAt(0)

        correctAnswer = nextImage
        txtQuestionNumber.text = getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ)
        txtAnswer.visibility = View.INVISIBLE

        val region: String = nextImage.substring(0, nextImage.indexOf('-'))

        val assets: AssetManager = requireActivity().assets

        try {
            val stream: InputStream = assets.open(region + '/' + nextImage + ".png")

            var flag: Drawable = Drawable.createFromStream(stream, nextImage)
            imgFlag.setImageDrawable(flag)
        } catch (exception: IOException) {
            Log.e(TAG, "Error loading" + nextImage, exception)
        }

        Collections.shuffle(fileNameList)

        val correct: Int = fileNameList.indexOf(correctAnswer)
        fileNameList.add(fileNameList.removeAt(correct))

        for (row in 0..(guessRows - 1)) {
            for (column in 0..(guessLinearLayouts[row].childCount - 1)) {
                val newBtnGuess: Button = guessLinearLayouts[row].getChildAt(column) as Button
                newBtnGuess.isEnabled = true

                var fileName = fileNameList.get(row * 2 + column)
                newBtnGuess.text = getCountryName(fileName)
            }
        }

        val row: Int = random.nextInt(guessRows)
        val column: Int = random.nextInt(2)
        val randomRow: LinearLayout = guessLinearLayouts[row]
        val countryName: String = getCountryName(correctAnswer)
        (randomRow.getChildAt(column) as Button).text = countryName
    }

    private fun getCountryName(name: String): String {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ')
    }

    private fun animate(animateOut: Boolean): Unit {
        if (correctAnswers == 0) return
        val centerX: Int = (quizLinearLayout.left + quizLinearLayout.right) / 2
        val centerY: Int = (quizLinearLayout.top + quizLinearLayout.bottom) / 2
        val radius: Int = Math.max(quizLinearLayout.width, quizLinearLayout.height)

        var animator: Animator

        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(
                quizLinearLayout,
                centerX,
                centerY,
                radius.toFloat(),
                0.0f
            )
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loadNextFlag()
                }
            })
        } else {
            animator = ViewAnimationUtils.createCircularReveal(
                quizLinearLayout,
                centerX,
                centerY,
                0.0f,
                radius.toFloat()
            )
        }

        animator.duration = 500
        animator.start()
    }
}
