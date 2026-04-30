import type { SelectHTMLAttributes } from 'react';
import type { UseFormRegisterReturn } from 'react-hook-form';

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
  registration?: UseFormRegisterReturn;
  options: Array<{ value: string | number; label: string }>;
}

export function SelectField({ label, error, registration, options, ...rest }: SelectFieldProps) {
  return (
    <label className="form-field">
      <span>{label}</span>
      <select {...registration} {...rest}>
        <option value="">Выберите</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error ? <small>{error}</small> : null}
    </label>
  );
}
