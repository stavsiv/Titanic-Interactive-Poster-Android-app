package com.example.titanicmovie

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView

//Displays a dialog with Titanic facts and a SeekBar for user interaction.

class FactsDialog(private val context: Context) {
    private val titanicFacts = listOf(
        context.getString(R.string.fact_1),
        context.getString(R.string.fact_2),
        context.getString(R.string.fact_3),
        context.getString(R.string.fact_4),
        context.getString(R.string.fact_5),
        context.getString(R.string.fact_6),
        context.getString(R.string.fact_7),
        context.getString(R.string.fact_8),
        context.getString(R.string.fact_9),
        context.getString(R.string.fact_10),
        context.getString(R.string.fact_11),
        context.getString(R.string.fact_12),
    )

    fun showTitanicFactsDialog() {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_titanic_facts, null)
        val factTextView = dialogView.findViewById<TextView>(R.id.factTextView)
        val knownFactsSeekBar = dialogView.findViewById<SeekBar>(R.id.knownFactsSeekbar)
        val knownFactsTextView = dialogView.findViewById<TextView>(R.id.knownFactsTextView)
        val feedbackTextView = dialogView.findViewById<TextView>(R.id.feedbackTextView)

        knownFactsSeekBar.max = titanicFacts.size - 1
        knownFactsSeekBar.progress = 0

        updateFactsDisplay(factTextView, 0)
        knownFactsTextView.text = context.getString(R.string.fact_text, 1, titanicFacts.size)
        feedbackTextView.text = context.getString(R.string.feedback_start)

        knownFactsSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateFactsDisplay(factTextView, progress)
                knownFactsTextView.text = context.getString(R.string.fact_text, progress + 1, titanicFacts.size)

                feedbackTextView.text = when {
                    progress == 0 -> context.getString(R.string.feedback_start)
                    progress < 4 -> context.getString(R.string.feedback_some)
                    progress < 8 -> context.getString(R.string.feedback_impressive)
                    else -> context.getString(R.string.feedback_expert)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.back_to_home_page)) { _, _ ->
                val knownCount = knownFactsSeekBar.progress + 1
                val message = when {
                    knownCount == 1 -> context.getString(R.string.finish_message_new)
                    knownCount == titanicFacts.size -> context.getString(R.string.finish_message_all)
                    else -> context.getString(R.string.finish_message_some, knownCount, titanicFacts.size)
                }

                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            .show()
    }

    private fun updateFactsDisplay(factTextView: TextView, highlightIndex: Int) {
        val factsView = StringBuilder()
        for (i in titanicFacts.indices) {
            if (i == highlightIndex) {
                factsView.append("<b>").append(i + 1).append(". ").append(titanicFacts[i])
                    .append("</b><br><br>")
            } else {
                factsView.append(i + 1).append(". ").append(titanicFacts[i]).append("<br><br>")
            }
        }
        factTextView.text = Html.fromHtml(factsView.toString(), Html.FROM_HTML_MODE_LEGACY)
    }
}