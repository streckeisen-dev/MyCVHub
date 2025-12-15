import { FormEvent, ReactNode, useState } from 'react'
import { ProfileThemeDto } from '@/types/ProfileThemeDto.ts'
import { useTranslation } from 'react-i18next'
import { ColorState } from 'react-color'
import { addToast, Button, Form, Navbar, NavbarBrand, NavbarContent } from '@heroui/react'
import { h4 } from '@/styles/primitives.ts'
import { ColorPicker } from '@/components/ColorPicker.tsx'
import { ProfileThemeUpdateDto } from '@/types/ProfileThemeUpdateDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { ProfileTheme } from '@/config/ProfileTheme.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { RestError } from '@/types/RestError.ts'
import { getMatchingTextColor } from '@/styles/TextColor.ts'

export type ThemeEditorProps = Readonly<{
  initialValue: ProfileThemeDto | undefined
}>

export function ThemeEditor(props: ThemeEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue } = props

  const [backgroundColor, setBackgroundColor] = useState<string>(
    initialValue?.backgroundColor ?? ProfileTheme.background
  )
  const [surfaceColor, setSurfaceColor] = useState<string>(
    initialValue?.surfaceColor ?? ProfileTheme.header
  )
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [isResetting, setIsResetting] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleBackgroundColorChange(color: ColorState) {
    setBackgroundColor(color.hex)
  }

  function handleSurfaceColorChange(color: ColorState) {
    setSurfaceColor(color.hex)
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    await save(backgroundColor, surfaceColor)
  }

  async function save(bgColor: string, surfaceColor: string, isReset = false) {
    if (isReset) {
      setIsResetting(true)
    } else {
      setIsSaving(true)
    }

    const update: ProfileThemeUpdateDto = {
      surfaceColor: surfaceColor,
      backgroundColor: bgColor
    }

    try {
      await ProfileApi.saveTheme(update, i18n.language)
      addToast({
        title: t('theme.saveSuccess'),
        timeout: 2500,
        color: 'success'
      })
    } catch (e) {
      const error = (e as RestError).errorDto
      const messages = error?.errors ?? {}
      setErrorMessages(messages)
      if (Object.keys(messages).length === 0) {
        addToast({
          title: t('theme.saveError'),
          description: error?.message ?? t('error.genericMessage'),
          color: 'danger'
        })
      }
    } finally {
      if (isReset) {
        setIsResetting(false)
      } else {
        setIsSaving(false)
      }
    }
  }

  async function handleReset() {
    const bg = ProfileTheme.background
    const surface = ProfileTheme.header
    setBackgroundColor(bg)
    setSurfaceColor(surface)
    await save(bg, surface, true)
  }

  return (
    <section className="flex flex-col gap-6">
      <p className="whitespace-break-spaces">{t('theme.description')}</p>

      <Form className="grid grid-cols-12 gap-6" onSubmit={handleSubmit}>
        <div className="col-span-12 lg:col-span-6 xl:col-span-4 grid grid-cols-12">
          <ColorPicker
            className="col-span-12 md:col-span-6"
            label={t('fields.backgroundColor')}
            isRequired
            color={backgroundColor}
            onChangeComplete={handleBackgroundColorChange}
            errorMessage={errorMessages.backgroundColor}
          />
          <ColorPicker
            className="col-span-12 md:col-span-6"
            label={t('fields.surfaceColor')}
            isRequired
            color={surfaceColor}
            onChangeComplete={handleSurfaceColorChange}
            errorMessage={errorMessages.surfaceColor}
          />
        </div>
        <div className="col-span-12 lg:col-span-6 xl:col-span-6">
          <h4 className={h4()}>{t('theme.preview')}</h4>

          <Navbar
            maxWidth="full"
            style={{
              backgroundColor: surfaceColor
            }}
          >
            <NavbarContent justify="start">
              <NavbarBrand>
                <p
                  style={{
                    color: getMatchingTextColor(surfaceColor)
                  }}
                >
                  {t('fields.surfaceColor')}
                </p>
              </NavbarBrand>
            </NavbarContent>
          </Navbar>
          <div
            style={{
              backgroundColor: backgroundColor,
              height: '50px'
            }}
          >
            <p
              className="p-3"
              style={{
                color: getMatchingTextColor(backgroundColor)
              }}
            >
              {t('fields.backgroundColor')}
            </p>
          </div>
        </div>
        <div className="col-span-12 flex gap-2">
          <Button
            type="submit"
            color="primary"
            isLoading={isSaving}
            isDisabled={isSaving || isResetting}
          >
            {t('forms.save')}
          </Button>
          <Button
            className="min-w-50"
            type="reset"
            onPress={handleReset}
            isLoading={isResetting}
            isDisabled={isResetting || isSaving}
          >
            {t('theme.reset')}
          </Button>
        </div>
      </Form>
    </section>
  )
}
