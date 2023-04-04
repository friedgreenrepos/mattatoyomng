package com.example.mattatoyomng

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.databinding.EventItemViewBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.mattatoyomng.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.YearMonth
class CalendarFragment : Fragment() {

//    private val eventsAdapter = EventAdapter {
//        AlertDialog.Builder(requireContext())
//            .setMessage(R.string.dialog_delete_confirmation)
//            .setPositiveButton(R.string.delete) { _, _ ->
//                deleteEvent(it)
//            }
//            .setNegativeButton(R.string.close, null)
//            .show()
//    }

    val titleRes: Int = 3
    val activityToolbar: Toolbar
        get() = binding.activityToolbar

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    private lateinit var binding: FragmentCalendarBinding

    //    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_calendar, container, false)
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentCalendarBinding.bind(view)
//        binding.calendarRV.apply {
//            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//            adapter = eventsAdapter
//            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
//        }
//        binding.calendarView.monthScrollListener = {
//            activityToolbar.title = if (it.yearMonth.year == today.year) {
//                titleSameYearFormatter.format(it.yearMonth)
//            } else {
//                titleFormatter.format(it.yearMonth)
//            }
//            // Select the first day of the visible month.
//            selectDate(it.yearMonth.atDay(1))
//        }
//        val daysOfWeek = daysOfWeek()
//        val currentMonth = YearMonth.now()
//        val startMonth = currentMonth.minusMonths(50)
//        val endMonth = currentMonth.plusMonths(50)
//        configureBinders(daysOfWeek)
//        binding.calendarView.apply {
//            setup(startMonth, endMonth, daysOfWeek.first())
//            scrollToMonth(currentMonth)
//        }
//
//        if (savedInstanceState == null) {
//            // Show today's events initially.
//            binding.calendarView.post { selectDate(today) }
//        }
//        binding.exThreeAddButton.setOnClickListener { inputDialog.show() }

    }
}