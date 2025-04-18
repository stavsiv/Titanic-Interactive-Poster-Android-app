import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.titanicmovie.R

// Displays a ticket order confirmation dialog

class TicketDetailsDialog(
    private val context: Context,
    private val cinema: String,
    private val screening: String,
    private val adultTickets: String,
    private val childTickets: String,
    private val date: String,
    private val phoneNumber: String
) {
    fun showTicketDetailsDialog() {
        val messageText = StringBuilder()
            .append(context.getString(R.string.cinema, cinema)).append("\n")
            .append(context.getString(R.string.screening, screening)).append("\n")
            .append(context.getString(R.string.date, date)).append("\n")
            .append(context.getString(R.string.phone_number, phoneNumber)).append("\n")
            .append(context.getString(R.string.adult_tickets, adultTickets)).append("\n")
            .append(context.getString(R.string.child_tickets, childTickets))
            .toString()

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.ticket_order_details))
            .setMessage(messageText)
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
