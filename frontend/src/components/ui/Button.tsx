import { Button as HeroButton, Spinner } from '@heroui/react'
import { ComponentProps, ElementType, ReactNode } from 'react'

type HeroButtonProps = ComponentProps<typeof HeroButton>
type PressHandler = NonNullable<HeroButtonProps['onPress']>
type ButtonVariant = NonNullable<HeroButtonProps['variant']>

export type ButtonProps = Omit<HeroButtonProps, 'children' | 'onPress'> & {
  as?: ElementType
  children?: ReactNode
  to?: string
  onPress?: (event?: Parameters<PressHandler>[0]) => void
}

export function Button(props: ButtonProps) {
  const {
    as: Component,
    children,
    className,
    isDisabled,
    isPending,
    to,
    variant = 'secondary',
    onPress,
    ...rest
  } = props

  const content = (
    <>
      {isPending && (
        <span data-testid="button-pending-indicator">
          <Spinner color="current" size="sm" />
        </span>
      )}
      {children}
    </>
  )

  const normalizedVariant = variant as ButtonVariant
  const roundedClass = 'rounded-full'
  const buttonClassName = [
    'min-h-10 gap-2 px-4 py-2 text-sm font-medium',
    roundedClass,
    className
  ]
    .filter(Boolean)
    .join(' ')

  if (Component) {
    const componentClassName = [
      'inline-flex min-h-10 items-center justify-center gap-2 px-4 py-2 text-sm font-medium transition-colors',
      roundedClass,
      normalizedVariant === 'primary' && 'bg-accent text-accent-foreground',
      normalizedVariant === 'danger' && 'bg-danger text-danger-foreground',
      normalizedVariant === 'danger-soft' && 'bg-danger/10 text-danger hover:bg-danger/15',
      normalizedVariant === 'tertiary' && 'bg-transparent hover:bg-default/10',
      normalizedVariant === 'secondary' && 'bg-default text-default-foreground',
      normalizedVariant === 'outline' && 'border border-default-300 bg-transparent hover:bg-default/10',
      normalizedVariant === 'ghost' && 'bg-transparent text-foreground hover:bg-default/10',
      className
    ]
      .filter(Boolean)
      .join(' ')

    return (
      <Component {...rest} className={componentClassName} onClick={() => onPress?.()} to={to}>
        {content}
      </Component>
    )
  }

  return (
    <HeroButton
      {...rest}
      className={buttonClassName || undefined}
      isDisabled={isDisabled || isPending}
      isPending={isPending}
      onPress={(event) => onPress?.(event)}
      variant={normalizedVariant}
    >
      {() => content}
    </HeroButton>
  )
}
