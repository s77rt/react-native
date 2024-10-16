/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.textinput;

import android.text.InputFilter;
import android.text.Spanned;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFilter implements InputFilter {
  private Pattern mPattern;

  public RegexFilter(String pattern) {
    mPattern = Pattern.compile(pattern);
  }

  @Override
  public CharSequence filter(
      CharSequence source,
      int start,
      int end,
      Spanned dest,
      int dstart,
      int dend) {
    StringBuilder newText = new StringBuilder(dest);
    if (start == 0 && end == source.length())
      newText.replace(dstart, dend, source.toString());
    else
      newText.replace(dstart, dend, source.subSequence(start, end).toString());
    Matcher matcher = mPattern.matcher(newText);
    if (matcher.matches()) {
      return null;
    }
    if (dend - dstart == 0) {
      return "";
    }
    return dest.subSequence(dstart, dend);
  }
}
