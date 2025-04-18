package com.example.titanicmovie

import TicketDetailsDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*

//Defines constant keys for storing order details.

object OrderKeys {
    const val CINEMA = "cinema_key"
    const val SCREENING = "screening_key"
    const val ADULT_TICKETS = "adult_tickets_key"
    const val CHILD_TICKETS = "child_tickets_key"
    const val DATE = "date_key"
    const val PHONE_NUMBER = "phone_number_key"
}

//Manages the last order details and displays them in a dialog.

object OrderDataManager {
    var lastOrderDetails: Map<String, String>? = null

    fun showOrderDetails(context: Context) {
        val lastOrder = lastOrderDetails
        if (lastOrder != null) {
            TicketDetailsDialog(
                context,
                lastOrder[OrderKeys.CINEMA] ?: "",
                lastOrder[OrderKeys.SCREENING] ?: "",
                lastOrder[OrderKeys.ADULT_TICKETS] ?: "",
                lastOrder[OrderKeys.CHILD_TICKETS] ?: "",
                lastOrder[OrderKeys.DATE] ?: "",
                lastOrder[OrderKeys.PHONE_NUMBER] ?: ""
            ).showTicketDetailsDialog()
        } else {
            showNoOrderMessage(context)
        }
    }

    fun showNoOrderMessage(context: Context) {
        val orientation = context.resources.configuration.orientation
        val message = context.getString(R.string.no_confirmed_order)

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            AlertDialog.Builder(context)
                .setTitle(message)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class OrderTicketsDialog(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_order_tickets, null)
        val adultSpinner = dialogView.findViewById<Spinner>(R.id.adultSpinner)
        val childSpinner = dialogView.findViewById<Spinner>(R.id.childSpinner)
        val totalPriceLabel = dialogView.findViewById<TextView>(R.id.priceLabel)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val cinemaGroup = dialogView.findViewById<RadioGroup>(R.id.cinemaGroup)
        val screeningGroup = dialogView.findViewById<RadioGroup>(R.id.screeningGroup)
        val purchaseButton = dialogView.findViewById<Button>(R.id.purchaseButton)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editTextPhone)
        val viewOrderButton = dialogView.findViewById<Button>(R.id.btnViewOrder)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        val adultPrice = 40
        val childPrice = 25

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val ticketQuantities = (0..10).toList()
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ticketQuantities).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        adultSpinner.adapter = adapter
        childSpinner.adapter = adapter

        setSpinnerListener(adultSpinner) {
            updateTotalPrice(adultSpinner, childSpinner, totalPriceLabel, adultPrice, childPrice)
        }
        setSpinnerListener(childSpinner) {
            updateTotalPrice(adultSpinner, childSpinner, totalPriceLabel, adultPrice, childPrice)
        }

        dateButton.setOnClickListener {
            showDatePicker(dateButton)
        }

//Purchase confirmation dialog: Shows a summary of the order and plays a sound upon confirmation.

        purchaseButton.setOnClickListener {
            try {
                val errors = validatePurchase(
                    cinemaGroup,
                    screeningGroup,
                    adultSpinner,
                    childSpinner,
                    dateButton,
                    phoneEditText
                )

                if (errors.isNotEmpty()) {
                    AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.error))
                        .setMessage(errors.joinToString("\n"))
                        .setPositiveButton(R.string.ok) { d, _ -> d.dismiss() }
                        .show()
                    return@setOnClickListener
                }

                val phoneNumber = phoneEditText.text.toString()
                val selectedCinema = getSelectedCinema(cinemaGroup)
                val selectedScreening = getSelectedScreening(screeningGroup)
                val adultTickets = adultSpinner.selectedItem.toString()
                val childTickets = childSpinner.selectedItem.toString()
                val selectedDate = dateButton.text.toString()

                val confirmMessage = StringBuilder()
                    .append(context.getString(R.string.adult_tickets, adultTickets)).append("\n")
                    .append(context.getString(R.string.child_tickets, childTickets)).append("\n")
                    .append(context.getString(R.string.cinema, selectedCinema)).append("\n")
                    .append(context.getString(R.string.screening, selectedScreening)).append("\n")
                    .append(context.getString(R.string.date, selectedDate))
                    .toString()

                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirm_purchase))
                    .setMessage(confirmMessage)
                    .setPositiveButton(context.getString(R.string.confirm)) { d, _ ->
                        d.dismiss()
                        try {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer.create(context, R.raw.purchase_sound)
                            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                            mediaPlayer?.start()
                        } catch (e: Exception) {
                            Log.e("OrderTicketsDialog", "Error initializing MediaPlayer: ${e.message}")
                        }

                        OrderDataManager.lastOrderDetails = mapOf(
                            OrderKeys.CINEMA to selectedCinema,
                            OrderKeys.SCREENING to selectedScreening,
                            OrderKeys.ADULT_TICKETS to adultTickets,
                            OrderKeys.CHILD_TICKETS to childTickets,
                            OrderKeys.DATE to selectedDate,
                            OrderKeys.PHONE_NUMBER to phoneNumber
                        )

                        AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.a_transaction_was_made))
                            .setMessage(
                                context.getString(R.string.ticket_success) + "\n\n" +
                                        context.getString(R.string.phone_number, phoneNumber)
                            )
                            .setPositiveButton(context.getString(R.string.ok)) { dialogTickets, _ ->
                                dialogTickets.dismiss()
                                OrderDataManager.showOrderDetails(context)
                                dialog.dismiss()
                            }
                            .show()
                    }
                    .setNegativeButton(context.getString(R.string.cancel)) { d, _ -> d.dismiss() }
                    .show()
            } catch (e: Exception) {
                Log.e("OrderTicketsDialog", "Error in purchase process: ${e.message}", e)
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.error))
                    .setMessage(context.getString(R.string.error))
                    .setPositiveButton(R.string.ok) { d, _ -> d.dismiss() }
                    .show()
            }
        }

        viewOrderButton.setOnClickListener {
            OrderDataManager.showOrderDetails(context)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showDatePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(context, R.style.DatePickerTheme),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                button.text = formattedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun validateDate(date: String): Boolean {
        val currentDate = Calendar.getInstance().time
        val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)
        return selectedDate.after(currentDate)
    }

    private fun updateTotalPrice(
        adultSpinner: Spinner,
        childSpinner: Spinner,
        totalPriceLabel: TextView,
        adultPrice: Int,
        childPrice: Int
    ) {
        val ticketAdult = adultSpinner.selectedItemPosition
        val ticketChild = childSpinner.selectedItemPosition
        val totalPrice = (ticketAdult * adultPrice) + (ticketChild * childPrice)
        totalPriceLabel.text = "$totalPrice ${context.getString(R.string.nis)}"
    }

    private fun setSpinnerListener(spinner: Spinner, onItemSelected: (() -> Unit)? = null) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected?.invoke()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getSelectedCinema(cinemaGroup: RadioGroup): String {
        val selectedRadioButtonId = cinemaGroup.checkedRadioButtonId
        if (selectedRadioButtonId == -1) {
            return "" // if no choice, return empty string
        }
        val selectedRadioButton = cinemaGroup.findViewById<RadioButton>(selectedRadioButtonId)
        return selectedRadioButton?.text?.toString() ?: ""
    }

    private fun getSelectedScreening(screeningGroup: RadioGroup): String {
        val selectedRadioButtonId = screeningGroup.checkedRadioButtonId
        if (selectedRadioButtonId == -1) {
            return "" // if no choice, return empty string
        }
        val selectedRadioButton = screeningGroup.findViewById<RadioButton>(selectedRadioButtonId)
        return selectedRadioButton?.text?.toString() ?: ""
    }

    private fun validatePurchase(
        cinemaGroup: RadioGroup,
        screeningGroup: RadioGroup,
        adultSpinner: Spinner,
        childSpinner: Spinner,
        dateButton: Button,
        phoneEditText: EditText
    ): List<String> {
        val errors = mutableListOf<String>()

        if (cinemaGroup.checkedRadioButtonId == -1) {
            errors.add(context.getString(R.string.no_cinema_selected))
        }

        if (screeningGroup.checkedRadioButtonId == -1) {
            errors.add(context.getString(R.string.no_screening_selected))
        }

        if (adultSpinner.selectedItemPosition == 0 && childSpinner.selectedItemPosition == 0) {
            errors.add(context.getString(R.string.no_tickets_selected))
        }

        val phoneNumber = phoneEditText.text.toString()
        if (phoneNumber.isEmpty() || !phoneNumber.matches(Regex("^\\d{10}$"))) {
            errors.add(context.getString(R.string.invalid_phone_number))
        }

        val selectedDate = dateButton.text.toString()
        if (selectedDate == context.getString(R.string.no_date_selected) ||
            selectedDate.isEmpty() ||
            dateButton.text == dateButton.hint) {
            errors.add(context.getString(R.string.no_date_selected))
        } else {
            try {
                if (!validateDate(selectedDate)) {
                    errors.add(context.getString(R.string.date_in_the_past))
                }
            } catch (e: Exception) {
                errors.add(context.getString(R.string.invalid_date_format))
            }
        }

        return errors
    }

}