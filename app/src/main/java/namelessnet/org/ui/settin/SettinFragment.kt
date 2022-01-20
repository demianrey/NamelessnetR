package namelessnet.org.ui.settin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import namelessnet.org.R

class SettinFragment : Fragment() {

    companion object {
        fun newInstance() = SettinFragment()
    }

    private lateinit var viewModel: SettinViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settin_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettinViewModel::class.java)
        // TODO: Use the ViewModel
    }

}