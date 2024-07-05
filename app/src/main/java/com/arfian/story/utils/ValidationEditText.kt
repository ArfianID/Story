package com.arfian.story.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.arfian.story.R

class ValidationEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private var validationType: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ValidationEditText,
            0, 0
        ).apply {
            try {
                validationType = getInt(R.styleable.ValidationEditText_validationType, 0)
            } finally {
                recycle()
            }
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (validationType) {
                    0 -> { // Email validation
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                            setError(context.getString(R.string.email_is_not_valid), null)
                        } else {
                            error = null
                        }
                    }

                    1 -> { // Password validation
                        if (s.toString().length < 8) {
                            setError(
                                context.getString(R.string.password_must_be_at_least_8_characters),
                                null
                            )
                        } else {
                            error = null
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })
    }
}