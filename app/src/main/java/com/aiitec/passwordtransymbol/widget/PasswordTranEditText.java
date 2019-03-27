package com.aiitec.passwordtransymbol.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;

import com.aiitec.passwordtransymbol.R;


/**
 * @Author: ailibin
 * @Time: 2019/03/27 输入密码类型的EditText的点形状变成“*”，看了源码之后，重写这两个类即可
 * @Description: EditText输入密码类型数据用*号遮挡,过渡的符号改变
 * @Email: ailibin@qq.com
 */
public class PasswordTranEditText extends android.support.v7.widget.AppCompatEditText {

    private static char charSymbol;

    public PasswordTranEditText(Context context) {
        this(context, null);
    }

    public PasswordTranEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordTranEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordTranEditText);
        String mSymbol = typedArray.getString(R.styleable.PasswordTranEditText_symbol);
        char[] chars = mSymbol.toCharArray();
        //重写获取一下焦点，重新定义之后，EditText失去了焦点。
        setFocusable(true);
        setFocusableInTouchMode(true);
        if (chars.length >= 1) {
            //数组大小大于等于1的只取首个字母
            charSymbol = chars[0];
        } else {
            //给点,这里用点的Unicode编码,转义一下就可以了
            charSymbol = '\u2022';
        }

        init();
        typedArray.recycle();

    }

    private void init() {
        //使用*号代替密码输入的过渡图标,默认是·
        setTransformationMethod(new AstPasswordTransformationMethod());
    }


    public static class AstPasswordTransformationMethod extends PasswordTransformationMethod {


        public AstPasswordTransformationMethod() {
        }

        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private static class PasswordCharSequence implements CharSequence {

            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                // Store char sequence
                mSource = source;
            }

            @Override
            public char charAt(int index) {
                // This is the important part
                return charSymbol;
            }

            @Override
            public int length() {
                // Return default
                return mSource.length();
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                // Return default
                return mSource.subSequence(start, end);
            }

        }
    }

}
