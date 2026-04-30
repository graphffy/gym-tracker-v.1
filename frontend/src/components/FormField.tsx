import type { InputHTMLAttributes, TextareaHTMLAttributes } from 'react';
import type { UseFormRegisterReturn } from 'react-hook-form';

type FieldProps = {
  label: string;
  error?: string;
  registration?: UseFormRegisterReturn;
  multiline?: false;
} & InputHTMLAttributes<HTMLInputElement>;

type TextAreaProps = {
  label: string;
  error?: string;
  registration?: UseFormRegisterReturn;
  multiline: true;
} & TextareaHTMLAttributes<HTMLTextAreaElement>;

export function FormField(props: FieldProps | TextAreaProps) {
  const { label, error, registration, multiline, ...rest } = props;
  return (
    <label className="form-field">
      <span>{label}</span>
      {multiline ? <textarea {...registration} {...(rest as TextareaHTMLAttributes<HTMLTextAreaElement>)} /> : <input {...registration} {...(rest as InputHTMLAttributes<HTMLInputElement>)} />}
      {error ? <small>{error}</small> : null}
    </label>
  );
}
