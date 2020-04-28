/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.ViewPropertyLimits;

/**
 * The <code>View</code> interface provides a coordinate transformation from model coordinates to eye coordinates. This
 * follows the OpenGL convention of a left-handed coordinate system with the origin at the eye point and looking down
 * the negative Z axis. <code>View</code> also provides a transformation from eye coordinates to screen coordinates,
 * following the OpenGL convention of an origin in the lower left hand screen corner.
 * <p/>
 * Most of the accessor and computation methods on <code>View</code> will use viewing state computed in the last call to
 * {@link #apply}.
 * <p/>
 * The following methods return state values <i>updated in the most recent call to apply</i>. <code> <ul>
 * <li>getEyePosition</li> <li>getEyePoint</li> <li>getUpVector</li> <li>getForwardVector</li>
 * <li>getModelviewMatrix</li> <li>getViewport</li> <li>getFrustum</li> <li>getFrustumInModelCoordinates</li>
 * <li>getProjectionMatrix</li> </code> </ul>
 * <p/>
 * The following methods return computed values using state that was updated in the most recent call to
 * <code>apply</code>. <code> <ul> <li>project</li> <li>unproject</li> <li>computeRayFromScreenPoint</li>
 * <li>computePositionFromScreenPoint</li> <li>computePixelSizeAtDistance</li> <li>computeHorizonDistance</li> </ul>
 * </code>
 *
 * @author Paul Collins
 * @version $Id: View.java 13727 2010-09-08 21:56:56Z tgaskins $
 * @see gov.nasa.worldwind.view.orbit.OrbitView
 */
public interface View extends WWObject, Restorable
{

    final String VIEW_STOPPED = "gov.nasa.worldwind.View.ViewStopped";

    /** Stops any movement associated with this <code>View</code>. */
    void stopMovement();

    /**
     * Returns the location of the eye in geographic coordinates. This value is computed in the most recent call to
     * <code>apply</code>.
     *
     * @return Position of the eye.
     */
    Position getEyePosition();

    /**
     * Sets the location of the eye in geographic coordinates. The implementation may interpret this command in whatever
     * way it chooses, so long as the eye is placed at the specified <code>eyePosition</code>.
     *
     * @param eyePosition Position of the eye.
     *
     * @throws IllegalArgumentException If <code>eyePosition</code> is null.
     */
    void setEyePosition(Position eyePosition);

    /**
     * Returns the most up-to-date location of the eye in geographic coordintes. Unlike {@link #getEyePosition} and
     * {@link #getEyePoint}, getCurrentEyePosition will return the View's immediate position.
     *
     * @return Position of the eye.
     */
    Position getCurrentEyePosition();

    /**
     * Sets the location of the eye, and the center of the screen in geographic coordinates. The implementation may
     * interpret this command in whatever way it choooses, so long as the eye is placed at the specified
     * <code>eyePosition</code>, and the center of the screen is the specified <code>centerPositoin</code>.
     * Specifically, implementations must determine what the up direction will be given these parameters, and apply
     * these parameters in a meaningful way.
     *
     * @param eyePosition    Position of they eye.
     * @param centerPosition Position of the screen center.
     */
    void setOrientation(Position eyePosition, Position centerPosition);

    /**
     * Sets the heading of the view.  The implementation may interpret this command in whatever way it chooses.
     *
     * @param heading The direction to aim the view in degrees
     */
    void setHeading(Angle heading);

    /**
     * Sets the pitch of the view.  The implemenation may interepret pitch as it chooses
     *
     * @param pitch The pitch of the view.
     */
    void setPitch(Angle pitch);

    /**
     * Returns the view's current heading.
     *
     * @return Angle of the view's heading.
     */
    Angle getHeading();

    /**
     * Returns the view's current pitch.
     *
     * @return Angle of the view's pitch.
     */
    Angle getPitch();

    /**
     * Returns the location of the eye in cartesian coordinates. This value is computed in the most recent call to
     * <code>apply</code>.
     *
     * @return Vec4 of the eye.
     */
    Vec4 getEyePoint();

    /**
     * Returns the most up-to-date location of the eye in cartesian coordinates. Unlike {@link #getEyePosition} and
     * {@link #getEyePoint}, getCurrentEyePoint will return the View's immediate position.
     *
     * @return Vec4 of the eye.
     */
    Vec4 getCurrentEyePoint();

    /**
     * Returns the up axis in cartesian coordinates. This value is computed in the most recent call to
     * <code>apply</code>.
     *
     * @return Vec4 of the up axis.
     */
    Vec4 getUpVector();

    /**
     * Returns the forward axis in cartesian coordinates. This value is computed in the most recent call to
     * <code>apply</code>.
     *
     * @return Vec4 of the forward axis.
     */
    Vec4 getForwardVector();

    /**
     * Returns the modelview matrix. The modelview matrix transforms model coordinates to eye coordinates. This matrix
     * is constructed using the model space translation and orientation specific to each the implementation. This value
     * is computed in the most recent call to <code>apply</code>.
     *
     * @return the current model-view matrix.
     */
    Matrix getModelviewMatrix();

    /**
     * Returns the horizontal field-of-view angle (the angle of visibility), or null if the implementation does not
     * support a field-of-view.
     *
     * @return Angle of the horizontal field-of-view, or null if none exists.
     */
    Angle getFieldOfView();

    /**
     * Sets the horiziontal field-of-view angle (the angle of visibillity) to the specified <code>fieldOfView</code>.
     * This may be ignored if the implementation that do not support a field-of-view.
     *
     * @param fieldOfView the horizontal field-of-view angle.
     *
     * @throws IllegalArgumentException If the implementation supports field-of-view, and <code>fieldOfView</code> is
     *                                  null.
     */
    void setFieldOfView(Angle fieldOfView);

    /**
     * Returns the bounds (x, y, width, height) of the viewport. The implementation will configure itself to render in
     * this viewport. This value is computed in the most recent call to <code>apply</code>.
     *
     * @return the Rectangle of the viewport.
     */
    java.awt.Rectangle getViewport();

    /**
     * Returns the near clipping plane distance, in eye coordinates.  Implementations of the <code>View</code> interface
     * are not required to have a method for setting the near and far distance. Applications that need to control the
     * near and far clipping distances can derive from {@link gov.nasa.worldwind.view.orbit.BasicOrbitView} or {@link
     * gov.nasa.worldwind.view.firstperson.BasicFlyView}
     *
     * @return near clipping plane distance, in eye coordinates.
     */
    double getNearClipDistance();

    /**
     * Returns the far clipping plane distance, in eye coordinates. Implementations of the <code>View</code> interface
     * are not required to have a method for setting the near and far distance. Applications that need to control the
     * near and far clipping distances can derive from {@link gov.nasa.worldwind.view.orbit.BasicOrbitView} or {@link
     * gov.nasa.worldwind.view.firstperson.BasicFlyView}
     *
     * @return far clipping plane distance, in eye coordinates.
     */
    double getFarClipDistance();

    /**
     * Returns the viewing <code>Frustum</code> in eye coordinates. The <code>Frustum</code> is the portion of viewable
     * space defined by three sets of parallel 'clipping' planes. This value is computed in the most recent call to
     * <code>apply</code>.
     *
     * @return viewing Frustum in eye coordinates.
     */
    Frustum getFrustum();

    /**
     * Returns the viewing <code>Frustum</code> in model coordinates. Model coordinate frustums are useful for
     * performing visibility tests against world geometry. This frustum has the same shape as the frustum returned in
     * <code>getFrustum</code>, but it has been transformed into model space. This value is computed in the most recent
     * call to <code>apply</code>.
     *
     * @return viewing Frustum in model coordinates.
     */
    Frustum getFrustumInModelCoordinates();

    /**
     * Gets the projection matrix. The projection matrix transforms eye coordinates to screen coordinates. This matrix
     * is constructed using the projection parameters specific to each implementation of <code>View</code>. The method
     * {@link #getFrustum} returns the geometry corresponding to this matrix. This value is computed in the most recent
     * call to <code>apply</code>.
     *
     * @return the current projection matrix.
     */
    Matrix getProjectionMatrix();

    /**
     * Calculates and applies this <code>View's</code> internal state to the graphics context in the specified
     * <code>dc</code>. All subsequently rendered objects use this new state. Upon return, the OpenGL graphics context
     * reflects the values of this view, as do any computed values of the view, such as the modelview matrix, projection
     * matrix and viewing frustum.
     *
     * @param dc the current World Wind DrawContext on which <code>View</code> will apply its state.
     *
     * @throws IllegalArgumentException If <code>dc</code> is null, or if the <code>Globe</code> or <code>GL</code>
     *                                  instances in <code>dc</code> are null.
     */
    void apply(DrawContext dc);

    /**
     * Maps a <code>Point</code> in model (cartesian) coordinates to a <code>Point</code> in screen coordinates. The
     * returned x and y are relative to the lower left hand screen corner, while z is the screen depth-coordinate. If
     * the model point cannot be sucessfully mapped, this will return null.
     *
     * @param modelPoint the model coordinate <code>Point</code> to project.
     *
     * @return the mapped screen coordinate <code>Point</code>.
     *
     * @throws IllegalArgumentException if <code>modelPoint</code> is null.
     */
    Vec4 project(Vec4 modelPoint);

    /**
     * Maps a <code>Point</code> in screen coordinates to a <code>Point</code> in model coordinates. The input x and y
     * are  relative to the lower left hand screen corner, while z is the screen depth-coordinate.  If the screen point
     * cannot be sucessfully mapped, this will return null.
     *
     * @param windowPoint the window coordinate <code>Point</code> to project.
     *
     * @return the mapped screen coordinate <code>Point</code>.
     *
     * @throws IllegalArgumentException if <code>windowPoint</code> is null.
     */
    Vec4 unProject(Vec4 windowPoint);

    /**
     * Defines and applies a new model-view matrix in which the world origin is located at <code>referenceCenter</code>.
     * Geometry rendered after a call to <code>pushReferenceCenter</code> should be transformed with respect to
     * <code>referenceCenter</code>, rather than the canonical origin (0, 0, 0). Calls to
     * <code>pushReferenceCenter</code> must be followed by {@link #popReferenceCenter} after rendering is complete.
     * Note that calls to {@link #getModelviewMatrix} will not return reference-center model-view matrix, but the
     * original matrix.
     *
     * @param dc              the current World Wind drawing context on which new model-view state will be applied.
     * @param referenceCenter the location to become the new world origin.
     *
     * @return a new model-view matrix with origin is at <code>referenceCenter</code>, or null if this method failed.
     *
     * @throws IllegalArgumentException if <code>referenceCenter</code> is null, if <code>dc</code> is null, or if the
     *                                  <code>Globe</code> or <code>GL</code> instances in <code>dc</code> are null.
     */
    Matrix pushReferenceCenter(DrawContext dc, Vec4 referenceCenter);

    /**
     * Removes the model-view matrix on top of the matrix stack, and restores the original matrix.
     *
     * @param dc the current World Wind drawing context on which the original matrix will be restored.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null, or if the <code>Globe</code> or <code>GL</code>
     *                                  instances in <code>dc</code> are null.
     */
    void popReferenceCenter(DrawContext dc);

    /**
     * Computes a line, in model coordinates, originating from the eye point, and passing throught the point contained
     * by (x, y) on the <code>View's</code> projection plane (or after projection into model space).
     *
     * @param x the horizontal coordinate originating from the left side of <code>View's</code> projection plane.
     * @param y the vertical coordinate originating from the top of <code>View's</code> projection plane.
     *
     * @return a line beginning at the <code>View's</code> eye point and passing throught (x, y) transformed into model
     *         space.
     */

    /**
     * Sets the reference center matrix without pushing the stack.
     *
     * @param dc              the drawing context.
     * @param referenceCenter the new reference center
     *
     * @return a new model-view matrix with origin is at <code>referenceCenter</code>, or null if this method failed.
     *
     * @throws IllegalArgumentException if <code>referenceCenter</code> is null, if <code>dc</code> is null, or if the
     *                                  <code>Globe</code> or <code>GL</code> instances in <code>dc</code> are null.
     * @see #pushReferenceCenter(gov.nasa.worldwind.render.DrawContext, gov.nasa.worldwind.geom.Vec4)
     */
    Matrix setReferenceCenter(DrawContext dc, Vec4 referenceCenter);

    Line computeRayFromScreenPoint(double x, double y);

    /**
     * Computes the intersection of a line originating from the eye point (passing throught (x, y)) with the last
     * rendered <code>SectorGeometry</code>, or the last analytical <code>Globe</code> if no rendered geometry exists.
     *
     * @param x the horizontal coordinate originating from the left side of <code>View's</code> projection plane.
     * @param y the vertical coordinate originating from the top of <code>View's</code> projection plane.
     *
     * @return the point on the surface in polar coordiantes.
     */
    Position computePositionFromScreenPoint(double x, double y);

    /**
     * Computes the dimension (in meters) that a screen pixel would cover at a given distance from the eye point (also
     * in meters). The distance is interpreted as the linear distance between the eye point and the world point in
     * question. This computation assumes that pixels dimensions are square, and therefore returns a single dimension.
     *
     * @param distance the distance in meters from the eye point. This value must be positive but is otherwise
     *                 unbounded.
     *
     * @return the dimension of a pixel in meters at the given distance.
     *
     * @throws IllegalArgumentException if <code>distance</code> is negative.
     */
    double computePixelSizeAtDistance(double distance);

    /**
     * Gets the distance from the <code>View's</code> eye point to the horizon point on the last rendered
     * <code>Globe</code>.
     * <p/>
     * Note: Use this method judiciously because it can be expensive. Call {@link #getFarClipDistance()} instead. It's
     * value is usually the same as the horizon distance.
     *
     * @return the distance from the eye point to the horizon.
     */
    double computeHorizonDistance();

    /**
     * Gets the center point of the view.
     *
     * @return the center point of the view if that point is on the globe, otherwise, return null
     */
    Vec4 getCenterPoint();

    /**
     * Gets the globe associated with this view. The Globe is updated at the beginning of each frame, during
     * <code>View.apply()</code>. <code>View.getGlobe()</code> returns null if called before the first invocation of
     * <code>View.apply()</code>.
     *
     * @return the globe being rendered by this view, or null before the first invocation of <code>View.apply()</code>.
     */
    Globe getGlobe();

    /**
     * Gets the <code>ViewInputHandler</code> being used to map input events to <code>View</code> controls.
     *
     * @return the <code>ViewInputHandler</code> being used to map input events to <code>View</code> controls.
     */
    ViewInputHandler getViewInputHandler();

    /** Stops any animations that are active in this <code>View</code> */
    void stopAnimations();

    /**
     * Animate to the specified position.  The implementation is expected to animate the <code>View</code> to look at
     * the given position from the given elevation.
     *
     * @param position  The position to animate to.
     * @param elevation The elevation to look at the <code>position</code> from.
     */
    void goTo(Position position, double elevation);

    /**
     * Determine if there are any animations active in the <code>View</code>.
     *
     * @return true if there are active animations, false otherwise.
     */
    boolean isAnimating();

    /**
     * Get the {@link ViewPropertyLimits} for this view.
     *
     * @return the {@link ViewPropertyLimits} for this view.
     */
    ViewPropertyLimits getViewPropertyLimits();

    /**
     * Copy the state of the given <code>View</code>.
     *
     * @param view The <code>View</code> whose state is to be copied.
     */
    void copyViewState(View view);

    /**
     * Add an animator to the <code>View</code>. This method does not start the {@link
     * gov.nasa.worldwind.animation.Animator}.  Starting the {@link gov.nasa.worldwind.animation.Animator} is the
     * responsibility of the application.
     *
     * @param animator the {@link gov.nasa.worldwind.animation.Animator} to be added
     */
    void addAnimator(Animator animator);
}
