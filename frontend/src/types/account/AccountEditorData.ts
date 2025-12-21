import { CalendarDate } from '@internationalized/date'

export interface AccountEditorData {
  username: string | undefined;
  firstName: string | undefined;
  lastName: string | undefined;
  email: string | undefined;
  phone: string | undefined;
  birthday: CalendarDate | null;
  street: string | undefined;
  houseNumber: string | undefined;
  postcode: string | undefined;
  city: string | undefined;
  country: string | undefined;
  language: string | undefined;
}
